package harvester.ui;

import harvester.RelatedKeyword;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class HarvesterTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -5057681513894708028L;
    private RelatedKeyword[] keywords = new RelatedKeyword[0];

    @Override
    public String getColumnName(int column) {
	switch (column) {
	case 0:
	    return "Word";

	case 1:
	    return "Relevance";
	}
	throw new IllegalArgumentException();
    }

    @Override
    public int getRowCount() {
	return keywords.length;
    }

    @Override
    public int getColumnCount() {
	return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
	RelatedKeyword rk = keywords[rowIndex];
	switch (columnIndex) {
	case 0:
	    return rk.getWord();
	case 1:
	    return rk.getRelevance();
	}
	throw new IllegalArgumentException();
    }

    synchronized void setData(RelatedKeyword[] keywords) {
	this.keywords = keywords;
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		fireTableDataChanged();
	    }
	});
    }
}
