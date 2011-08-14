/**
 * 
 */
package harvester;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author wallmanm
 * 
 */
public class HtmlElementFinderTest {

    private HtmlElementFinder testee;
    private String html;


    @Before
    public void setup() throws Exception {
	loadTestData();
	testee = new HtmlElementFinder(html);
    }

    private void loadTestData() throws IOException {
	InputStream in = getClass().getResourceAsStream("TestPage.html");
	html = IOUtils.toString(in);
	in.close();
    }

    @Test
    public void find_title() {
	assertEquals("This is my title", testee.getTitle());
    }

    @Test
    public void find_keywords_in_metatags() throws Exception {
	assertEquals("test, is, something, good", testee.getMetaValue());
    }

    @Test
    public void find_h1_words() throws Exception {
	assertEquals("This is a h1 paragraph Another h1 paragraph", testee.getBodyValues(ElementQualifier.H1));
    }
    
    @Test
    public void find_h2_words() throws Exception {
	assertEquals("This is a h2 paragraph", testee.getBodyValues(ElementQualifier.H2));
    }
    
    @Test
    public void find_h3_words() throws Exception {
	assertEquals("h3 med en href This is a h3 paragraph", testee.getBodyValues(ElementQualifier.H3));
    }
}
