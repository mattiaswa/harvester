package harvester;

import com.jgoodies.forms.factories.FormFactory
import com.jgoodies.forms.layout.ColumnSpec
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.layout.RowSpec

import javax.swing.border.TitledBorder
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField

class SearchAndResultPanel extends JPanel {
  val c1 = Array[ColumnSpec](FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC)
  val v2 = Array[RowSpec](FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), RowSpec.decode("max(100dlu;default):grow"), FormFactory.DEFAULT_ROWSPEC)

  setLayout(new FormLayout(c1, v2))

  val keywordTf = new JTextField();
  add(keywordTf, "2, 2, fill, default");
  keywordTf.setColumns(10);

  val submitBtn = new JButton("Submit");
  add(submitBtn, "4, 2, center, center");

  private val scrollPane = new JScrollPane();
  add(scrollPane, "2, 4, 3, 1, fill, fill");

  val table = new JTable();
  scrollPane.setViewportView(table);

  private val panel = new JPanel();
  panel.setBorder(new TitledBorder(null, "Log", TitledBorder.LEADING, TitledBorder.TOP, null, null));
  add(panel, "2, 5, 3, 1, fill, fill");
  
  panel.setLayout(new FormLayout(Array[ColumnSpec](ColumnSpec.decode("default:grow")), Array[RowSpec](RowSpec.decode("max(100dlu;default):grow"))))

  private val scrollPane_1 = new JScrollPane();
  panel.add(scrollPane_1, "1, 1, fill, fill");

  val loggingTa = new JTextArea();
  scrollPane_1.setViewportView(loggingTa);

  // textArea = new JTextArea();
  // add(textArea, "2, 6, 3, 1, fill, fill");

}
