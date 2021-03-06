/** Created by Zachary Helfinstein
 *  There is some sort of copyright on this material, I haven't decided what yet.
 *  So basically just don't use it at the moment, all right?
 *  
 *  Last edited: 10/19/13
 */

package zComics;

import java.io.IOException;
import java.util.logging.*;
import java.util.ArrayList;

//Servlet imports
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

//AppEngine imports
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

@SuppressWarnings("serial")
public class ComicChooser extends HttpServlet {

    //Use log.warning() to print to the log file/terminal
    private Logger log = Logger.getLogger(ComicChooser.class.getName());

    //Use these values instead of string literals so the compiler can catch typos
    public static final String XKCD = "xkcd", XKCD_URL = "http://xkcd.com", 
	SMBC = "Saturday Morning Breakfast Cereal", SMBC_URL= "http://www.smbc-comics.com",
	COMIC_NAME = "comic_name", COMIC_URL = "comic_url", CURRENT_COMIC= "current_comic", 
	URL_SUFFIX = "_url", URL_PROP = "url", COMIC_SETTINGS_KEY = "ComicSettings:",
	USER_KEY = "UserPrefs:", USER_PROP = "user", NOT_SUPPORTED = "/comicNotSupported.html",
	USER_COMIC_LIST = "MyComics";

    //Called whenever the correct url mapping is requested using "post"
    public void doPost(HttpServletRequest req, HttpServletResponse resp) 
	throws IOException, ServletException {
	
	if(req.getParameter(COMIC_NAME) == "login") {
	    resp.sendRedirect(UserServiceFactory.getUserService().createLoginURL("/"));
	    return;
	}

	//Get the user, datastore, and memcache
	UserService uServe = UserServiceFactory.getUserService();
	User user = uServe.getCurrentUser();
	DatastoreService dServe = DatastoreServiceFactory.getDatastoreService();
	MemcacheService mServe = MemcacheServiceFactory.getMemcacheService();
	Entity userPrefs = getUserPrefs(user, dServe, mServe);

	//Find out the name of the comic, and create a variable for the URL
	String newComicName = (String)req.getParameter(COMIC_NAME);
	String comicUrl="/";

	//Check if the comic name is null
	if(newComicName != null) {
	    //Check if the user has preferences. If not, treat them as if they are logged out.
	    if(userPrefs != null) {
		Comic myComic = (Comic)userPrefs.getProperty(CURRENT_COMIC);
		//Check if this comic is the user's current or not. If not, make it their current comic.
		if(myComic.getName() != newComicName) {
		    ComicKeeper ck = getComicSettings(newComicName, dServe, mServe);
		    myComic = new Comic(ck);
		    userPrefs.setProperty(CURRENT_COMIC, myComic);
		}
		comicUrl = myComic.getURL();

	    } else {
		//If there is no logged in user, find the default URL for the default comic (XKCD).
		Entity global = ComicOrganizerServlet.getGlobal(dServe,mServe);
		ComicKeeper ck = (ComicKeeper)(global.getProperty(XKCD));
		comicUrl = ck.getDefaultURL();
	    }
	} else {
	    //Because the comic name is null, say that it's not supported.
	    comicUrl = NOT_SUPPORTED;
	}

	//A final check if the URL is null (this should never happen, but because null causes 
	//all sorts of nasty errors, I check anyways
	if(comicUrl != null) {
	    //Put the user's preferences into the datastore and memcache (if they exist)
	    if(user != null) {
		String cacheKey = USER_KEY+user.getUserId();	    
		dServe.put(userPrefs);
		mServe.put(cacheKey, userPrefs);
	    }
	    //Redirect the page to whatever comic URL we found
	    resp.sendRedirect(comicUrl);

	} else {
	    //This case should never happen, so I indicate that in the logs.
	    log.warning("WTF???");
	}
	resp.sendRedirect("/");
    }


    //---------------------Methods------------------------


    //Returns the preferences for the given user. If the user null, returns null.
    //If no preferences exist for the user, it creates a new set of preferences with 
    //the properties USER_PROP = user, COMIC_NAME = XKCD, and COMIC_URL = XKCD_URL
    public static Entity getUserPrefs(User user, DatastoreService dServe, MemcacheService mServe) {

	//Checks if the user is null, and if so, returns that there are no userPrefs
	if(user == null) {
	    return null;
	}
	
	//Tries to get userPrefs from the memcache
	String cacheKey = USER_KEY+user.getUserId();
	Entity userPrefs = (Entity)mServe.get(cacheKey);
	
	//If it wasn't in the memcache, try to get it from the datastore
	if(userPrefs == null) {
	    Key userKey = KeyFactory.createKey(USER_KEY, user.getUserId());
	    try {
		userPrefs = dServe.get(userKey);
	    } catch(EntityNotFoundException e) {
		//The userPrefs aren't in the memcache or the datastore, so we make a new Entity
		//and give it default values.
		userPrefs = new Entity(userKey);
		userPrefs.setProperty(USER_PROP, user);
		userPrefs.setProperty(COMIC_NAME, XKCD);
		userPrefs.setProperty(COMIC_URL, XKCD_URL);
		//Put this new entity in both the datastore and memcache
		dServe.put(userPrefs);
		mServe.put(cacheKey, userPrefs);
	    }
	}
	//return what we found/made
	return userPrefs;
    }

    //Returns the settings for a given webcomic. If no settings exist, it returns null.
    public static ComicKeeper getComicSettings(String name, DatastoreService dServe, MemcacheService mServe) {
	
	//If the name is null, we know there's no point looking for settings
	if(name == null) {
	    return null;
	}
	
	Entity global = ComicOrganizerServlet.getGlobal(dServe, mServe);
	ComicKeeper result = (ComicKeeper)(global.getProperty(name));

	return result;
    }

    //Creates a "key" to identify the url for a specific comic in a user's preferences.
    public static String makeURLKey(String comicName) {
	return comicName+"_url";
    }

    //Because the datastore cannot store an array, this converts a tilde-deliminated String
    //into a String array.
    public static String[] makeStringArray(String in) {
	String[] array = new String[countOf(in, '~')+1];
	int begin = 0;
	int end = in.indexOf('~');
	int i;
		
	for(i = 0; end > 0; i++) {
	    array[i] = in.substring(begin, end);
	    begin = end + 1;
	    end = in.indexOf('~', begin);
	}

	end = in.length();
	array[i] = in.substring(begin, end);
	return array;
    }

    //Finds the count of a given character delimiter. Used in converting to/from String[]
    private static int countOf(String str, char del) {
	int total = 0;
	for(int i = 0; i < str.length(); i++) {
	    if(str.charAt(i) == del) {
		total++;
	    }
	}
	return total;
    }

    //Because the datastore cannot store arrays, this converts a String[] into a tilde-delimited String
    public static String makeString(String[] in) {
	String output = "";
	int i;
	for(i = 0; i < in.length - 1; i++) {
	    output += in[i];
	    output += "~";
	}
	output += in[i];
	return output;
    }

    //Given a user's preferences, a comic name, and the datastore and memcache, this method will
    //find the url, if it exists. If the user has a private url, it will return that. Otherwise,
    //it searches for the default url for that comic.
    //    public static String getComicURL(Entity userPrefs, String comicName, DatastoreService dServe, MemcacheService mServe){
    //=========================================================================================
    //                                   DEPRECATED
    //=========================================================================================



    //Given a comic name, datastore, and memcache, this method will find the default url
    //of that comic, if it exists
    public static String getComicURL(String comicName, DatastoreService dServe, 
				     MemcacheService mServe) {
	//If we cannot find anything, return NOT_SUPPORTED
	String url = NOT_SUPPORTED;
	ComicKeeper comicSettings = getComicSettings(comicName, dServe, mServe);
	if(comicSettings != null) {
	    url = comicSettings.getDefaultURL();
	}
	return url;
    }

    public static String makeIndexKey(int index) {
	return "HEY SO THE INDEX SHOULD BE "+index;
    }
}