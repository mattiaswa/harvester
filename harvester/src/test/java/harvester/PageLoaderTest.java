/**
 * 
 */
package harvester;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author mattias
 * 
 */
public class PageLoaderTest {
    private PageLoader testee;

    @Before
    public void setup() {
	testee = new PageLoader() {
	    @Override
	    protected InputStream createPageInputStream(String urlString) throws IOException {
	       String testPage = null;
	       if("http://www.soccer.com/".equals(urlString)) {
		   testPage = "soccer.html";
	       } else if("http://www.fifa.com/".equals(urlString)) {
		   testPage = "fifa.html";
	       } else {
		   return null;
	       }
	       
	       return PageLoaderTest.class.getResourceAsStream(testPage);
	    }	    
	};
	testee.setSearchProvider(new FakeSearchProvider());
    }
    
    @Test
    public void load_returns_proper_pages() throws Exception {
	Collection<HtmlPageData> pages = (Collection<HtmlPageData>) testee.getPagesForKeyword("soccer");
	
	assertEquals(2, pages.size());
	//Add more tests by peeking the content of the PageData
    }

    private class FakeSearchProvider extends SearchProvider {
	@Override
	public JsonObject searchForKeywordStartingAtSpecifiedIndex(String keyword, int startIndex) {
	    String searchPageName = null;
	    if (startIndex == 1) {
		searchPageName = "testSearchPage1.json";
	    } else if (startIndex == 11) {
		searchPageName = "testSearchPage2.json";
	    } else {
		fail("Undefined startIndex: " + startIndex);
	    }

	    return loadJsonDataFromResource(searchPageName);
	}

	private JsonObject loadJsonDataFromResource(String searchPageName) {
	    InputStreamReader pageReader = null;
	    try {
		pageReader = new InputStreamReader(PageLoaderTest.this.getClass().getResourceAsStream(searchPageName));

		return (JsonObject) new JsonParser().parse(pageReader);
	    } finally {
		IOUtils.closeQuietly(pageReader);
	    }
	}
    }
}
