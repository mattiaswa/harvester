/**
 * 
 */
package harvester;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author mattias
 * 
 */
class GoogleSearchResultExtractorUtility {

    public static String[] findUrls(JsonObject input) {
	JsonArray searchResults = (JsonArray) input.get("items");
	Set<String> urls = new LinkedHashSet<String>();
	for (Iterator<JsonElement> iter = searchResults.iterator(); iter.hasNext();) {
	    JsonElement elem = iter.next();
	    if (elem.isJsonObject()) {
		String link = ((JsonObject) elem).get("link").getAsString();
		urls.add(link);
	    }
	}

	return urls.toArray(new String[urls.size()]);
    }

    public static int extractNextStartIndex(JsonObject searchResult) {
	JsonElement queries = searchResult.get("queries");
	if (queries == null) {
	    throw new IllegalArgumentException("Invalid search result");
	}
	JsonElement nextPageElement = ((JsonObject) queries).get("nextPage");
	if (nextPageElement == null || nextPageElement.isJsonNull()) {
	    return -1;
	}
	return ((JsonObject) ((JsonArray) nextPageElement).get(0)).get("startIndex").getAsInt();
    }
}
