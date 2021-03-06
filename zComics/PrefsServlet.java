/** Created by Zachary Helfinstein
 *  There is some sort of copyright on this material, I haven't decided what yet.
 *  So basically just don't use it at the moment, all right?
 *  
 *  Last edited: 10/19/13
 */

package zComics;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

@SuppressWarnings("serial")
public class PrefsServlet extends HttpServlet {

    private Logger log = Logger.getLogger(PrefsServlet.class.getName());

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	UserService uServe = UserServiceFactory.getUserService();
	User user = uServe.getCurrentUser();

	DatastoreService dServe = DatastoreServiceFactory.getDatastoreService();
	MemcacheService mServe = MemcacheServiceFactory.getMemcacheService();
	Entity userPrefs = ComicChooser.getUserPrefs(user, dServe, mServe);
	String comicName = (String)userPrefs.getProperty(ComicChooser.COMIC_NAME);
	
	String oldComicList = (String)userPrefs.getProperty(ComicChooser.USER_COMIC_LIST);
	if(oldComicList == null || oldComicList.equals("")) {
	    String[] coms = {ComicChooser.XKCD, ComicChooser.SMBC};
	    userPrefs.setProperty(ComicChooser.USER_COMIC_LIST, ComicChooser.makeString(coms));
	}

	if(comicName == null) {
	    String tempName = (String)userPrefs.getProperty(ComicChooser.COMIC_NAME);
	    if(tempName == null || tempName.equals("")) {
		userPrefs.setProperty(ComicChooser.COMIC_NAME, ComicChooser.XKCD);
		userPrefs.setProperty(ComicChooser.COMIC_URL, ComicChooser.XKCD_URL);
		comicName = ComicChooser.XKCD;
		log.warning("Overriding request (or lack thereof) with XKCD)");
	    } else {
		resp.sendRedirect("/");
		return;
	    }
	}

	String urlKey = ComicChooser.makeURLKey(comicName);
	String comicURL = (String)req.getParameter(ComicChooser.COMIC_URL);
	log.warning("User property "+urlKey+": "+comicURL);
	if(comicURL == null || comicURL.equals("") || !(comicURL.substring(0,7).equals("http://"))) {
	    Entity comicSettings = ComicChooser.getComicSettings(comicName, dServe, mServe);
	    if(comicSettings != null) {
		comicURL = (String)comicSettings.getProperty(ComicChooser.COMIC_URL);
		if(comicURL == null || comicURL.equals("")) {
		    comicName = ComicChooser.XKCD;
		    comicURL = ComicChooser.XKCD_URL;
		    log.warning("Overriding request with XKCD");
		}
	    } else {
		comicName = ComicChooser.XKCD;
		comicURL = ComicChooser.XKCD_URL;
		log.warning("Overriding request with XKCD");
	    }
	}

	userPrefs.setProperty(urlKey, comicURL);
        dServe.put(userPrefs);
	mServe.put(ComicChooser.USER_KEY+user.getUserId(), userPrefs);
	resp.sendRedirect("/");
   }
}