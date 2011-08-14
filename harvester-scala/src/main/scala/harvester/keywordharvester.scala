package harvester

case class RelatedKeyword(word: String, relevance: Int) extends Comparable[RelatedKeyword] {
  def compareTo(o: RelatedKeyword): Int = {
    return o.relevance - this.relevance
  }
}

object ElementQualifier extends Enumeration {
  type ElementQualifier = Value
  val H1, H2, H3 = Value
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

class KeywordHarvester {
  var pageLoader : PageLoader = _
  var currentKeyword: String = null
  val wordCount = scala.collection.mutable.Map.empty[String, Int]

  def findRelatedKeywords(word: String): Array[RelatedKeyword] = {
    if (word == null || ("" == word.trim)) {
      return EMPTY_RESULT
    }
    try {
      this.currentKeyword = word
      collectRelatedKeywords(pageLoader.getPagesForKeyword(word))
//
      wordCount.toSet.map({entry : Pair[String, Int] => RelatedKeyword(entry._1, entry._2)}).toArray
    } finally {
      this.currentKeyword = null
      wordCount.clear
    }
  }

  private def collectRelatedKeywords(pages: Iterable[HtmlPageData]): Unit = {
    for (page <- pages) {
      collect(page, 5, {
        _.title.split(SPLIT_PATTERN)
      })
      collect(page, 2, {
        _.metaValue.split(SPLIT_PATTERN)
      })
      ElementQualifier.values.foreach({ q: ElementQualifier => collect(page, 1, { pageData: HtmlPageData => pageData.getBodyValue(q).split(SPLIT_PATTERN) }) })
    }
  }

  private def collect(page: HtmlPageData, weight: Int, transformer: (HtmlPageData => Array[String])): Unit = {
    for (string <- transformer(page)) {
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
    if (currentKeyword == trimmedString) {
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
    return sting.matches("\\d*\\.\\d+")
  }
}