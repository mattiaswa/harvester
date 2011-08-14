package harvester;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SearchProvider {
    
    private final static String SEARCH_URL_PATTERN = "https://www.googleapis.com/customsearch/v1?" + "cx=008238868649727884978:msi2xt9aatg"
	    + "&key=AIzaSyDqC8D822At_Sj2LumIu5a4au1J0gPEVB8&nu" + "&q=%s" + "&start=%d";

    public JsonObject searchForKeywordStartingAtSpecifiedIndex(String keyword, int startIndex) {
	try {
	    URL url = new URL(String.format(SEARCH_URL_PATTERN, URLEncoder.encode(keyword, "utf-8"), startIndex));
	    	    
	    return loadDataFromURL(url);
	} catch (MalformedURLException e) {
	    throw new HarvesterException(e);
	} catch (UnsupportedEncodingException e) {
	    throw new HarvesterException(e);
	}
    }

    protected JsonObject loadDataFromURL(URL url) {
	Reader reader = null;
	try {
	    URLConnection connection = url.openConnection();
	    reader = new InputStreamReader(connection.getInputStream());
	    JsonObject searchResult = (JsonObject) new JsonParser().parse(reader);

	    return searchResult;
	} catch (IOException e) {
	    throw new HarvesterException("Could not perform search", e);
	} finally {
	    IOUtils.closeQuietly(reader);
	}
    }  
}
