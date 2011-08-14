/**
 * 
 */
package harvester;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

/**
 * @author wallmanm
 *
 */
public class SearchProviderTest {
     @Before
    public void setUp() throws Exception {
	
    }

    @Test
    public void ensure_search_url_is_properly_escaped() {
	final String keyword = "ifk rules";
	SearchProvider testee = new SearchProvider() {
	    @Override
	    protected JsonObject loadDataFromURL(URL url) {
	        assertTrue(url.toString().contains("&q=ifk+rules"));
		return null;
	    }
	};
	testee.searchForKeywordStartingAtSpecifiedIndex(keyword, 1);
    }
    
    @Test
    public void ensure_search_url_contains_proper_start_value() throws Exception {
	SearchProvider testee = new SearchProvider() {
	    @Override
	    protected JsonObject loadDataFromURL(URL url) {
	        assertTrue(url.toString().contains("&start=666"));
		return null;
	    }
	};
	testee.searchForKeywordStartingAtSpecifiedIndex("dontcare", 666);
    }

}
