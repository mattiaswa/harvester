package harvester

class HarvesterException(message: String, t: Throwable) extends RuntimeException(message, t)

case class RelatedKeyword(word: String, relevance: Int) extends Comparable[RelatedKeyword] {
  def compareTo(o: RelatedKeyword): Int = {
    return o.relevance - this.relevance
  }
}

object ElementQualifier extends Enumeration {
  type ElementQualifier = Value
  val H1, H2, H3 = Value

  def valueOfTag(value: String) = H1
}

import harvester.ElementQualifier._

class HtmlPageData(val title: String, val metaValue: String, val bodyContent: Map[ElementQualifier, String]) {
  def getBodyValue(qualifier: ElementQualifier) = bodyContent.get(qualifier).getOrElse("")
  def getTitle = ""
  def getMetaValue = ""

}

object KeywordHarvester {
  val EMPTY_RESULT = Array.empty[RelatedKeyword]
  val STOP_WORDS = Set("and", "of", "for", "with", "or")
  val SPLIT_PATTERN = "[/, :;]";

}

import KeywordHarvester._

class KeywordHarvester(private val keyword: String) {
  var pageLoader: PageLoader = _
  val wordCount = scala.collection.mutable.Map.empty[String, Int]

  def findRelatedKeywords(): List[RelatedKeyword] = {
    if (keyword == null || ("" == keyword.trim)) {
      return List.empty
    }
    collectRelatedKeywords(pageLoader.getPagesForKeyword(keyword))
    wordCount.toSet.map({ entry: Pair[String, Int] => RelatedKeyword(entry._1, entry._2) }).toList
  }

  private def collectRelatedKeywords(pages: Iterable[HtmlPageData]): Unit = {
    for (page <- pages) {
      collect(page, 5, _.title)
      collect(page, 2, _.metaValue)
      ElementQualifier.values.foreach({ q: ElementQualifier => collect(page, 1, _.getBodyValue(q)) })
    }
  }

  private def collect(page: HtmlPageData, weight: Int, transformer: (HtmlPageData => String)): Unit = {
    for (string <- transformer(page).split(SPLIT_PATTERN)) {
      val trimmedString = string.trim.toLowerCase
      if (isStringValid(trimmedString)) {
        wordCount.remove(trimmedString) match {
          case Some(value) => wordCount.put(trimmedString, value + weight)
          case None => wordCount.put(trimmedString, weight)
        }
      }
    }
  }

  private def isStringValid(trimmedString: String): Boolean = {
    if (trimmedString == null || 0 == trimmedString.length) {
      return false
    }
    if (keyword == trimmedString) {
      return false
    }
    if (STOP_WORDS.contains(trimmedString)) {
      return false
    }
    if (trimmedString.length < 3) {
      return false
    }
    if (isDigit(trimmedString)) {
      return false
    }
    return true
  }

  private def isDigit(sting: String): Boolean = {
    return sting.matches("\\d*\\.?\\d+")
  }
}