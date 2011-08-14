package harvester

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder

import scala.collection.JavaConversions._

import org.apache.commons.io.IOUtils._
import org.apache.commons.io._

import com.google.gson._

import SearchProvider._
import harvester.ElementQualifier._
import net.htmlparser.jericho.CharacterReference
import net.htmlparser.jericho.HTMLElementName
import net.htmlparser.jericho.MasonTagTypes
import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes
import net.htmlparser.jericho.PHPTagTypes
import net.htmlparser.jericho.Source
import net.htmlparser.jericho.TextExtractor

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

  val source = new Source(page);
  source.fullSequentialParse();

  val title = getTitle(source)
  val keywords = getMetaValue(source, "keywords")
  val bodyContent = searchBody(source)

  private def searchBody(source: Source) = {
    var body = Map.empty[ElementQualifier, String]
    for (htmlTagName <- ElementQualifier.values) {

      source.getAllElements(htmlTagName.toString().toLowerCase()).foreach(hTag => {
        val content = new TextExtractor(hTag).toString();
        body.get(htmlTagName) match {
          case None => body += htmlTagName -> content
          case Some(value) => body += htmlTagName -> (value + " " + content)
        }
      })
    }
    body
  }

  def getBodyValues(q: ElementQualifier) = {
    bodyContent.get(q);
  }

  private def getTitle(source: Source) = {
    val titleElement = source.getFirstElement(HTMLElementName.TITLE)
    if (titleElement == null) {
      "";
    }

    // TITLE element never contains other tags so just decode it collapsing
    // whitespace:
    CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
  }

  private def getMetaValue(source: Source, key: String) = {
    var pos = 0
    while (pos < source.length()) {
      val startTag = source.getNextStartTag(pos, "name", key, false);
      if (startTag == null)
        null;
      if (startTag.getName().equals(HTMLElementName.META)) {
        // Attribute values are automatically decoded
        startTag.getAttributeValue("content");
      }
      pos = startTag.getEnd();
    }
    "";
  }
}

private object SearchProvider {
  val SEARCH_URL_PATTERN = "https://www.googleapis.com/customsearch/v1?" + "cx=008238868649727884978:msi2xt9aatg&key=AIzaSyDqC8D822At_Sj2LumIu5a4au1J0gPEVB8&nu" + "&q=%s" + "&start=%d"
}

class SearchProvider {
    
    def searchForKeywordStartingAtSpecifiedIndex(keyword : String, startIndex : Int) = {
	try {
	    val url = new URL(format(SEARCH_URL_PATTERN, URLEncoder.encode(keyword, "utf-8"), startIndex));
	    	    
	    loadDataFromURL(url);
	} catch (MalformedURLException e) {
	    throw new HarvesterException(e);
	} catch (UnsupportedEncodingException e) {
	    throw new HarvesterException(e);
	}
    }

    def loadDataFromURL(url : URL) {
	var reader : Reader = null;
	try {
	    val connection = url.openConnection();
	    reader = new InputStreamReader(connection.getInputStream())
	    val searchResult = (JsonObject) (new JsonParser().parse(reader))

	    return searchResult;
	} catch (IOException e) {
	    throw new HarvesterException("Could not perform search", e);
	} finally {
	    IOUtils.closeQuietly(reader);
	}
    }  
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

    new HtmlPageData(finder.title, finder.keywords, finder.bodyContent)
  }

  protected def createPageInputStream(urlString: String): Option[InputStream] = {
    try {
      Some(new URL(urlString).openConnection().getInputStream())
    } catch {
      case e: IOException => None
    }
  }
}