package harvester

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

import org.apache.commons.io.IOUtils._
import org.apache.commons.io._

import harvester.ElementQualifier._
import com.google.gson._;

private object PageLoader {
  val NUMBER_OF_PAGES_TO_FETCH = 10;
}

private object HtmlElementFinder {
  MicrosoftConditionalCommentTagTypes.register();
	PHPTagTypes.register();
	PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags for this
	// example otherwise they override
	// processing instructions
	MasonTagTypes.register();
}
private class HtmlElementFinder(private val page: String) {
//  def getTitle = ""
//  def getMetaValue = ""
//  def getBodyValues: Map[ElementQualifier, String] = Map.empty[ElementQualifier, String]
//  
  
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

class SearchProvider {
  def searchForKeywordStartingAtSpecifiedIndex(keyword: String, pageStart: Int): JsonObject = null
}

object GoogleSearchResultExtractorUtility {
  def findUrls(searchResult: JsonObject) = List.empty[String]
  def extractNextStartIndex(data: JsonObject) = 0
}

object VoidPageLoaderReporter extends PageLoaderReporter

trait PageLoaderReporter {
  def report(message: String): Unit = println("Reportin " + message)
}

class PageLoader(reporter: PageLoaderReporter) {
  def this() = this(VoidPageLoaderReporter)

  var searchProvider: SearchProvider = null

  def getPagesForKeyword(keyword: String): Seq[HtmlPageData] = collectUrlsFromSearchResult(keyword).map(loadPageFromUrl).flatten;

  private def collectUrlsFromSearchResult(keyword: String) = {
    def collectUrlsFromSearchResult(keyword: String, start: Int, result: List[String]): List[String] = {
      if (result.size > 10) {
        result
      }
      val searchResult = searchProvider.searchForKeywordStartingAtSpecifiedIndex(keyword, start)
      collectUrlsFromSearchResult(keyword, GoogleSearchResultExtractorUtility.extractNextStartIndex(searchResult), result ::: GoogleSearchResultExtractorUtility.findUrls(searchResult))
    }
    //    var collectedUrls = List.empty[String];
    //
    //    var pageStart = 1
    //    for (collectedUrlsCnt <- List.range(1, 10) if pageStart > 0) {
    //      val searchResult = searchProvider.searchForKeywordStartingAtSpecifiedIndex(keyword, pageStart)
    //
    //      collectedUrls = collectedUrls ::: GoogleSearchResultExtractorUtility.findUrls(searchResult)
    //      pageStart = GoogleSearchResultExtractorUtility.extractNextStartIndex(searchResult);
    //    }
    //    collectedUrls
    collectUrlsFromSearchResult(keyword, 1, List.empty[String])

  }

  private def loadPageFromUrl(urlString: String) = {
    createPageInputStream(urlString) match {
      case None => {
        reporter.report(String.format("Could not load data from %s, skipping", urlString))
        None
      }

      case Some(input) => {
        this.reporter.report(String.format("Loading data from %s", urlString));
        val res = Some(createHtmlFromPageContent(IOUtils.toString(input)))
        closeQuietly(input);
        res
      }
    }

  }
  //
  private def createHtmlFromPageContent(pageAsString: String) = {
    val finder = new HtmlElementFinder(pageAsString);

    new HtmlPageData(finder.getTitle, finder.getMetaValue, finder.getBodyValues);
  }

  protected def createPageInputStream(urlString: String): Option[InputStream] = {
    try {
      Some(new URL(urlString).openConnection().getInputStream())
    } catch {
      case e: IOException => None
    }
  }
}