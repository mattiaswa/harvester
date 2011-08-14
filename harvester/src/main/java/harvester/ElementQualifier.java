package harvester;

import java.util.ArrayList;
import java.util.List;

enum ElementQualifier {
    H1("h1"), H2("h2"), H3("h3");
    
    public final String htmlTag;

    private ElementQualifier(String htmlTag) {
	this.htmlTag = htmlTag;
    }

    public static String[] asStrings() {
	List<String> ret = new ArrayList<String>();
	for (ElementQualifier eq : ElementQualifier.values()) {
	    ret.add(eq.htmlTag);
	}
	return ret.toArray(new String[ret.size()]);
    }
    
    public static ElementQualifier valueOfTag(String value) {
	for (ElementQualifier eq : values()) {
	    if(eq.htmlTag.equalsIgnoreCase(value)) {
		return eq;
	    }
	}
	throw new EnumConstantNotPresentException(ElementQualifier.class, value); 
    }
}
