/**
 * 
 */
package harvester;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author mattias
 * 
 */
public class KeywordHarvesterTest  {
    private KeywordHarvester testee;

    @Before
    public void setup() {
	testee = new KeywordHarvester();	
    }

    @Test
    public void search_for_keyword_results_in_related_words() throws Exception {
	testee.setPageLoader(createPageLoaderReturningContent(new HtmlPageData("umts, nokia, gsm", "umts, nokia",bodyContent("umts"))));
	
	RelatedKeyword[] foundKeywords = testee.findRelatedKeywords("mobile phone");
	RelatedKeyword[] expectedKeywords = new RelatedKeyword[] { new RelatedKeyword("umts", 8), new RelatedKeyword("nokia", 7),
		new RelatedKeyword("gsm", 5) };

	assertArrayEquals(expectedKeywords, foundKeywords);
    }
    
    @Test
    public void digits_does_not_contribute() throws Exception {
	testee.setPageLoader(createPageLoaderReturningContent(new HtmlPageData("5, 5.3, .53, 1.", "5,23",bodyContent(""))));
	
	RelatedKeyword[] foundKeywords = testee.findRelatedKeywords("dontcare");
	RelatedKeyword[] expectedKeywords = new RelatedKeyword[0];

	assertArrayEquals(expectedKeywords, foundKeywords);	
    }
    
    @Test
    public void words_shorter_than_three_dont_contribute() throws Exception {
	testee.setPageLoader(createPageLoaderReturningContent(new HtmlPageData("as, er, et", "",bodyContent(""))));
	
	RelatedKeyword[] foundKeywords = testee.findRelatedKeywords("dontcare");
	RelatedKeyword[] expectedKeywords = new RelatedKeyword[0];

	assertArrayEquals(expectedKeywords, foundKeywords);	
	
    }
    
    private PageLoader createPageLoaderReturningContent(HtmlPageData htmlPageData) {
	PageLoader loader = mock(PageLoader.class);
	
	when(loader.getPagesForKeyword(anyString())).thenReturn(Collections.singleton(htmlPageData));
	
	return loader;
    }

    @Test
    public void stop_words_contribute_nothing() throws Exception {
	HtmlPageData pageWithStopWords = new HtmlPageData("Title for title and title", "with, of", bodyContent("This or this"));	
	testee.setPageLoader(createPageLoaderReturningContent(pageWithStopWords));
	
	RelatedKeyword[] foundKeyword = testee.findRelatedKeywords("blaha");
	RelatedKeyword[] expectedKeywords = new RelatedKeyword[] { new RelatedKeyword("title", 15), new RelatedKeyword("this", 2)};
	
	assertArrayEquals(expectedKeywords, foundKeyword);
    }

    @Test
    public void search_for_empty_string_results_in_empty_related_keywords() throws Exception {
	assertEquals(0, testee.findRelatedKeywords("").length);
    }
    
    @Test
    public void content_is_cleared_between_consecutive_calls() throws Exception {
	testee.setPageLoader(createPageLoaderReturningContent(new HtmlPageData("sdasd, asddf", "", bodyContent(""))));
	
	RelatedKeyword[] expected = new RelatedKeyword[]{new RelatedKeyword("asddf", 5), new RelatedKeyword("sdasd", 5)};
	RelatedKeyword[] result = testee.findRelatedKeywords("fsdf");
	
	assertArrayEquals(expected, result);
	
	RelatedKeyword[] secondCallResult = testee.findRelatedKeywords("fsdf");
	assertArrayEquals(expected, secondCallResult);
    }

    private static Map<ElementQualifier, String> bodyContent(String content) {
	Map<ElementQualifier, String> body = new HashMap<ElementQualifier, String>();
	body.put(ElementQualifier.H1, content);
	return body;
    }
}
