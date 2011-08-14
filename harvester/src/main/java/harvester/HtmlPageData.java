/**
 * 
 */
package harvester;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wallmanm
 * 
 */
class HtmlPageData {

    private final String title;
    private final String metaValue;
    private final Map<ElementQualifier, String> bodyContent = new HashMap<ElementQualifier, String>() {
	private static final long serialVersionUID = 1L;

	public String get(Object key) {
	    if (!containsKey(key)) {
		return "";
	    }
	    return super.get(key);
	};
    };

    public HtmlPageData(String title, String metaValue, Map<ElementQualifier, String> bodyContent) {
	this.title = title == null ? "" : title;
	this.metaValue = metaValue == null ? "" : metaValue;
	this.bodyContent.putAll(bodyContent);
    }

    public String getTitle() {
	return title;
    }

    public String getMetaValue() {
	return metaValue;
    }

    public String getBodyValue(ElementQualifier qualifier) {
	return bodyContent.get(qualifier);
    }
}
