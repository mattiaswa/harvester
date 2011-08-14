package harvester.ui;

import harvester.KeywordHarvester;
import harvester.PageLoader;
import harvester.PageLoaderReporter;
import harvester.RelatedKeyword;
import harvester.SearchProvider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

public class HarvesterApp extends JFrame implements ActionListener, PageLoaderReporter {

    private static final long serialVersionUID = 8885620142972678840L;
    private final HarvesterTableModel tableModel;
    private final SearchAndResultPanel panel;
    private final KeywordHarvester harvester;

    public HarvesterApp() {
	PageLoader loader = new PageLoader(this);
	SearchProvider sp = new SearchProvider();
	loader.setSearchProvider(sp);

	harvester = new KeywordHarvester();
	harvester.setPageLoader(loader);

	panel = new SearchAndResultPanel();
	panel.submitBtn.addActionListener(this);

	panel.table.setModel(tableModel = new HarvesterTableModel());
	getContentPane().add(panel);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setTitle("Keyword harvester");
    }

    public static void main(String[] args) {
	JFrame frame = new HarvesterApp();
	frame.pack();
	frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	final String keyword = panel.keywordTf.getText();
	new Thread() {
	    @Override
	    public void run() {
		tableModel.setData(new RelatedKeyword[0]);
		
		RelatedKeyword[] res = harvester.findRelatedKeywords(keyword);
		tableModel.setData(res);
	    }
	}.start();
    }

    @Override
    public void progress(final String message) {
	Runnable r = new Runnable() {
	    public void run() {
		try {
		    panel.loggingTa.getDocument().insertString(panel.loggingTa.getDocument().getLength(), message + "\n", null);
		} catch (BadLocationException e) {
		}
	    }
	};
	SwingUtilities.invokeLater(r);
    }

}
