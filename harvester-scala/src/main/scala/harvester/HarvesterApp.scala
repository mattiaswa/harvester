package harvester;

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import javax.swing.JFrame

object HarvesterApp extends JFrame with ActionListener with PageLoaderReporter {
  val loader = new PageLoader(this)
  loader.searchProvider = new SearchProvider

  val tableModel = new HarvesterTableModel
  val panel = new SearchAndResultPanel
  panel.submitBtn.addActionListener(this);

  panel.table.setModel(tableModel);
  getContentPane().add(panel);
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  setTitle("Keyword harvester");

  def main(args: Array[String]) {
    HarvesterApp.pack();
    HarvesterApp.setVisible(true);
  }

  override def actionPerformed(e: ActionEvent) = {
    val harvester = new KeywordHarvester(panel.keywordTf.getText)
    harvester.pageLoader = loader

    new Thread {
      override def run = {
        tableModel.setData(Array.empty[RelatedKeyword]);
        tableModel.setData(harvester.findRelatedKeywords.toArray)
      }
    }.start
  }
  
  def progress(message: String) = 
    MySwingUtilities.invokeLater(panel.loggingTa.getDocument().insertString(panel.loggingTa.getDocument().getLength(), message + "\n", null))
}
