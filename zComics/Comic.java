/**COMMENT

This is my code, don't steal it.
*/

package zComics;

import java.io.*;
import java.util.Scanner;


public class Comic implements Serializable {

    int index;
    String currentURL;
    ComicKeeper comic;
    
    public Comic(ComicKeeper theComic) {
	comic = theComic;
	index = 0;
	currentURL = comic.getDefaultURL();
    }
    public Comic(){}

    public void setURL(String url) {
	if(url.length() > 6 && url.substring(0,7).equals("http://")) {
	    index = comic.findURL(url);
	    if(index >= 0) {
		currentURL = url;
	    }    
	}
    }

    public String getURL() {
	return currentURL;
    }

    public int getIndex() {
	return index;
    }
    
    public void next() {
	currentURL = comic.getNextURL(index);
	index = comic.findURL(currentURL);
    }

    public String getName() {
	return comic.getName();
    }


    public String toString() {
	String result = "";
	result += currentURL;
	result += "\n";
	result += comic.toString();
	result += "\n";
	
	return result;
    }

    public static Comic fromString(String in) {
	Scanner scn = new Scanner(in);
	if(!scn.hasNextLine()) {
	    return null;
	}
	String currentURL = scn.nextLine();
	String pass = "";
	while(scn.hasNextLine()) {
	    pass += scn.nextLine();
	}
	ComicKeeper ck = ComicKeeper.fromString(pass);
	Comic result =  new Comic(ck);
	result.setURL(currentURL);
	
	return result;
    }

    //For Serializable
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	out.writeInt(index);
	out.writeChars(currentURL);
	out.writeObject(comic);
    }

    //For Serializable
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	index = in.readInt();
	currentURL = in.readUTF();
	comic = (ComicKeeper)in.readObject();
    }

    //For Serializable
    private void readObjectNoData() throws ObjectStreamException {
	index = 0;
	currentURL = "/";
	comic = null;
    }

}