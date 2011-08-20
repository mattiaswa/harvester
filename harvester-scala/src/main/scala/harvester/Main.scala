package harvester
import com.google.gson.JsonParser

object Main {

  def main(args: Array[String]): Unit = { 
//    println(new JsonParser().parse(scala.io.Source.fromURL("https://www.googleapis.com/customsearch/v1?" + "cx=008238868649727884978:msi2xt9aatg"
//	    + "&key=AIzaSyDqC8D822At_Sj2LumIu5a4au1J0gPEVB8&nu" + "&q=mobile+phone" + "&start=1").mkString))
//    exit()
//    
    val harvester = new KeywordHarvester("mobile phone")
    val loader = new PageLoader
    val provider = new SearchProvider
    
    loader.searchProvider = provider
    harvester.pageLoader = loader
    
    println(harvester.findRelatedKeywords)
  }

}