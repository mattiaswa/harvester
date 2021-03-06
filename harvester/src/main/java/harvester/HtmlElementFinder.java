/**
 * 
 */
package harvester;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.TextExtractor;

/**
 * @author wallmanm
 * 
 */
class HtmlElementFinder {

    static {
	MicrosoftConditionalCommentTagTypes.register();
	PHPTagTypes.register();
	PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags for this
	// example otherwise they override
	// processing instructions
	MasonTagTypes.register();
    }

    private final String title;
    private final String keywords;
    private final Map<ElementQualifier, String> bodyContent = new HashMap<ElementQualifier, String>();

    public HtmlElementFinder(String htmlSource) {
	Source source = new Source(htmlSource);
	source.fullSequentialParse();

	this.title = getTitle(source);
	this.keywords = getMetaValue(source, "keywords");
	searchBody(source); 
    }

    private void searchBody(Source source) {
	final Set<String> tagNames = new HashSet<String>(Arrays.asList(ElementQualifier.asStrings()));

	for (String htmlTagName : tagNames) {
	    for (Element hTag : source.getAllElements(htmlTagName)) {
		TextExtractor te = new TextExtractor(hTag);
		updateBodyContent(htmlTagName, te.toString());
	    }
	}	
    }

    public String getTitle() {
	return title;
    }

    public String getMetaValue() {
	return keywords;
    }

    public String getBodyValues(ElementQualifier q) {
	return bodyContent.get(q);
    }
    
    public Map<ElementQualifier, String> getBodyValues() {
	return new HashMap<ElementQualifier, String>(bodyContent);
    }

    private void updateBodyContent(String elemName, String content) {
	ElementQualifier eq = ElementQualifier.valueOfTag(elemName);
	
	String oldValue = bodyContent.get(eq);
	if(oldValue == null) {
	    bodyContent.put(eq, content);
	} else {
	    bodyContent.put(eq, oldValue + " " + content);
	}
    }

    private String getTitle(Source source) {
	Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
	if (titleElement == null) {
	    return "";
	}

	// TITLE element never contains other tags so just decode it collapsing
	// whitespace:
	return CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
    }

    private static String getMetaValue(Source source, String key) {
	for (int pos = 0; pos < source.length();) {
	    StartTag startTag = source.getNextStartTag(pos, "name", key, false);
	    if (startTag == null)
		return null;
	    if (startTag.getName().equals(HTMLElementName.META)) {
		// Attribute values are automatically decoded
		return startTag.getAttributeValue("content");
	    }
	    pos = startTag.getEnd();
	}
	return "";
    }

}
