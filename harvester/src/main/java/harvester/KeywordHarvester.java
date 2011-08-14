package harvester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

public class KeywordHarvester {
    private final static RelatedKeyword[] EMPTY_RESULT = new RelatedKeyword[0];

    private PageLoader pageLoader;

    private final Map<String, Integer> wordCount = new HashMap<String, Integer>();
    private String currentKeyword;

    private static final Set<String> STOP_WORDS = new HashSet<String>(Arrays.asList("and", "of", "for", "with", "or"));

    private static final String SPLIT_PATTERN = "[/, :;]";

    public synchronized RelatedKeyword[] findRelatedKeywords(String word) {
	if (word == null || "".equals(word.trim())) {
	    return EMPTY_RESULT;
	}

	try {
	    this.currentKeyword = word;
	    collectRelatedKeywords(pageLoader.getPagesForKeyword(word));

	    return createResult();
	} finally {
	    this.currentKeyword = null;
	    wordCount.clear();
	}
    }

    private RelatedKeyword[] createResult() {
	List<RelatedKeyword> result = new ArrayList<RelatedKeyword>();
	for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
	    result.add(new RelatedKeyword(entry.getKey(), entry.getValue()));
	}

	Collections.sort(result);
	return result.toArray(new RelatedKeyword[result.size()]);
    }

    private void collectRelatedKeywords(Iterable<HtmlPageData> pages) {
	for (HtmlPageData page : pages) {
	    collect(page, 5, new Transformer<HtmlPageData, String[]>() {
		public String[] transform(HtmlPageData input) {
		    return input.getTitle().split(SPLIT_PATTERN);
		}
	    });
	    collect(page, 2, new Transformer<HtmlPageData, String[]>() {
		public String[] transform(HtmlPageData input) {
		    return input.getMetaValue().split(SPLIT_PATTERN);
		}
	    });
	    collectBodyWords(page, 1);
	}
    }

    private void collectBodyWords(HtmlPageData source, int weight) {
        for (final ElementQualifier qualifier : ElementQualifier.values()) {
            collect(source, weight, new Transformer<HtmlPageData, String[]>() {
                public String[] transform(HtmlPageData input) {
                    return input.getBodyValue(qualifier).split(SPLIT_PATTERN);
                }
            });
        }
    }

    private void collect(HtmlPageData page, int weight, Transformer<HtmlPageData, String[]> transformer) {
	for (String string : transformer.transform(page)) {
	    String trimmedString = string.trim().toLowerCase();
	    if (isStringValid(trimmedString)) {
		Integer previous = wordCount.remove(trimmedString);
		if (previous == null) {
		    wordCount.put(trimmedString, weight);
		} else {
		    previous += weight;
		    wordCount.put(trimmedString, previous);
		}
	    }
	}
    }

    private boolean isStringValid(String trimmedString) {
	if (trimmedString == null || 0 == trimmedString.length()) {
	    return false;
	}

	if (currentKeyword.equals(trimmedString)) {
	    return false;
	}

	if (STOP_WORDS.contains(trimmedString)) {
	    return false;
	}

	if (trimmedString.length() < 3) {
	    return false;
	}

	if (isDigit(trimmedString)) {
	    return false;
	}

	return true;
    }

    private boolean isDigit(String sting) {
	return sting.matches("\\d*\\.\\d+");
    }

    public void setPageLoader(PageLoader pageLoader) {
	this.pageLoader = pageLoader;
    }
}
