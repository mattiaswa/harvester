package harvester;

import javax.swing.table.AbstractTableModel
import javax.swing.SwingUtilities

import scala.collection.JavaConversions._
object HarvesterTableModel{}
class HarvesterTableModel extends AbstractTableModel {
  var keywords = Array.empty[RelatedKeyword]

  override def getColumnName(column: Int) = {
    column match {
      case 0 => "Word"
      case 1 => "Relevance"
    }
  }

  def getRowCount = keywords.size

  def getColumnCount = 2

  
  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    columnIndex match {
      case 0 => keywords(rowIndex).word
      case 1 => keywords(rowIndex).relevance.asInstanceOf[Object]
    }
  }

  def setData(keywords: Array[RelatedKeyword]) {
    this.keywords = keywords;
    MySwingUtilities.invokeLater(fireTableDataChanged)
  }
}

object MySwingUtilities {
  def invokeLater[X](exp: => X) {
    import javax.swing.SwingUtilities

    SwingUtilities invokeLater (new Runnable() {
      def run = exp
    })
  }
}
