package harvester;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GoogleSearchResultExtractorUtilityTest {

    private JsonObject input;

    @Before
    public void setup() {
	input = (JsonObject) new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream(
		"GoogleSearchResultExtractorUtilityTestData.json")));
    }

    @Test
    public void findUrlsFromSearchContent() throws Exception {
	String[] expectedUrls = new String[] { new String("http://reviews.cnet.com/cell-phones/"), new String("http://www.mobileburn.com/") };

	assertArrayEquals(expectedUrls, GoogleSearchResultExtractorUtility.findUrls(input));
    }

    @Test
    public void findStartIndexForNextPage() throws Exception {
	assertEquals(11, GoogleSearchResultExtractorUtility.extractNextStartIndex(input));
    }
}
