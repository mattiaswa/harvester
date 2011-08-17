package harvester

import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder

import scala.collection.JavaConversions._
import scala.util.control.Exception._

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
    var body = Map[ElementQualifier, String]()
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

  def searchForKeywordStartingAtSpecifiedIndex(keyword: String, startIndex: Int) = {
    catching(classOf[MalformedURLException], classOf[UnsupportedEncodingException]) opt {
      loadDataFromURL(format(SEARCH_URL_PATTERN, URLEncoder.encode(keyword, "utf-8"), startIndex));
    }
  }

  def loadDataFromURL(url: String) = new JsonParser().parse(scala.io.Source.fromURL(url).bufferedReader()).asInstanceOf[JsonObject]
}

object GoogleSearchResultExtractorUtility {
  def findUrls(input: JsonObject) = {
    input.get("items").asInstanceOf[JsonArray].filter(elem => elem.isJsonObject() && elem.isInstanceOf[JsonObject]).map(_.asInstanceOf[JsonObject].get("link").getAsString).toList
  }

  def extractNextStartIndex(searchResult: JsonObject) = {
    val nextPageElement = searchResult.get("queries").asInstanceOf[JsonObject].get("nextPage")
    if (nextPageElement == null || nextPageElement.isJsonNull()) {
      -1
    } else {
      nextPageElement.asInstanceOf[JsonArray].get(0).asInstanceOf[JsonObject].get("startIndex").getAsInt
    }
  }
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
      if (start < 0 || result.size > 10) {
        result
      }

      searchProvider.searchForKeywordStartingAtSpecifiedIndex(keyword, start) match {
        case None => result
        case Some(searchResult) => collectUrlsFromSearchResult(keyword, GoogleSearchResultExtractorUtility.extractNextStartIndex(searchResult), result ::: GoogleSearchResultExtractorUtility.findUrls(searchResult))
      }
    }

    collectUrlsFromSearchResult(keyword, 1, List[String]())
  }

  private def loadPageFromUrl(urlString: String) = {
    catching(classOf[IOException]) opt {
      val finder = new HtmlElementFinder(scala.io.Source.fromURL(urlString).mkString);
      new HtmlPageData(finder.title, finder.keywords, finder.bodyContent)
    }
  }

}