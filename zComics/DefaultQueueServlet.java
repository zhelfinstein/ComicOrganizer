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
import javax.servlet.http.Cookie;

import java.util.Enumeration;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
//import com.google.appengine.api.users.User;
//import com.google.appengine.api.users.UserService;
//import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

@SuppressWarnings("serial")
public class DefaultQueueServlet extends HttpServlet {

    private Logger log = Logger.getLogger(PrefsServlet.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
	log.warning("getAuthType(): "+req.getAuthType());
	log.warning("getContextPath(): "+req.getContextPath());
	Cookie[] cookies = req.getCookies();
	for(int i = 0; i < cookies.length; i++) {
	    log.warning("getCookies()["+i+"]: "+cookies[i]);
	}
	
	Enumeration<String> headers = req.getHeaderNames();
	for(String hdr = headers.nextElement(); headers.hasMoreElements(); hdr = headers.nextElement()) {
	    log.warning("getHeader("+hdr+"): "+req.getHeader(hdr));
	}

	log.warning("getMethod(): "+req.getMethod());
	log.warning("getPathInfo(): "+req.getPathInfo());
	log.warning("getPathTranslated(): "+req.getPathTranslated());
	log.warning("getQueryString(): "+req.getQueryString());
	log.warning("getRemoteUser(): "+req.getRemoteUser());
	log.warning("getRequestedSessionId(): "+req.getRequestedSessionId());
	log.warning("getRequestURI(): "+req.getRequestURI());
	log.warning("getRequestURL(): "+req.getRequestURL());
	log.warning("getServletPath(): "+req.getServletPath());

	resp.setStatus(HttpServletResponse.SC_OK);
    }
}