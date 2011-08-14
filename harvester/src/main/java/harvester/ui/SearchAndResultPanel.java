package harvester.ui;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SearchAndResultPanel extends JPanel {
    private static final long serialVersionUID = -4289208250681111093L;

    JTextField keywordTf;
    private JScrollPane scrollPane;
    JTable table;
    JButton submitBtn;
    JTextArea loggingTa;
    private JPanel panel;
    private JScrollPane scrollPane_1;

    /**
     * Create the panel.
     */
    public SearchAndResultPanel() {
	setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
		FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
		FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
		RowSpec.decode("default:grow"), RowSpec.decode("max(100dlu;default):grow"), FormFactory.DEFAULT_ROWSPEC, }));

	keywordTf = new JTextField();
	add(keywordTf, "2, 2, fill, default");
	keywordTf.setColumns(10);

	submitBtn = new JButton("Submit");
	add(submitBtn, "4, 2, center, center");

	scrollPane = new JScrollPane();
	add(scrollPane, "2, 4, 3, 1, fill, fill");

	table = new JTable();
	scrollPane.setViewportView(table);

	panel = new JPanel();
	panel.setBorder(new TitledBorder(null, "Log", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	add(panel, "2, 5, 3, 1, fill, fill");
	panel.setLayout(new FormLayout(new ColumnSpec[] {
			ColumnSpec.decode("default:grow"),},
		new RowSpec[] {
			RowSpec.decode("max(100dlu;default):grow"),}));
	
	scrollPane_1 = new JScrollPane();
	panel.add(scrollPane_1, "1, 1, fill, fill");

	loggingTa = new JTextArea();
	scrollPane_1.setViewportView(loggingTa);

	// textArea = new JTextArea();
	// add(textArea, "2, 6, 3, 1, fill, fill");
    }
}
