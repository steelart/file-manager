package steelart.alex.filemanager.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import steelart.alex.filemanager.FMElementCollection;
import steelart.alex.filemanager.ElementColumnProperty;
import steelart.alex.filemanager.FMUtils;
import steelart.alex.filemanager.FMElement;
import steelart.alex.filemanager.FMEnterable;

/**
 * Swing panel implementation
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public class SFMPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final List<ElementColumnProperty> collumns = Arrays.asList(ElementColumnProperty.NAME, ElementColumnProperty.SIZE);
    private JTable table;

    public SFMPanel(FMElementCollection start) {
        super(new GridLayout(1,0));

        resetTable(start);
    }

    private static List<FMElement> getElementList(Collection<FMElement> elements) {
        return FMUtils.getSortedList(elements, ElementColumnProperty.NAME, false);
    }

    private void resetTable(FMElementCollection newDir) {
        if (table != null) {
            remove(table);
        }

        FMPanelModel pm = new FMPanelModel(newDir);
        table = new JTable(pm);

        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);

        table.setCellSelectionEnabled(true);
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        final ListSelectionModel cellSelectionModel = table.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {

                int[] selectedRow = table.getSelectedRows();
                int selected = selectedRow[0];

                FMElement element = pm.elements.get(selected);
                FMEnterable entareble = element.asEnterable();
                if (entareble != null) {
                    removeAll();
                    revalidate();
                    repaint();
                    resetTable(entareble.enter());
                    revalidate();
                    repaint();
                }
            }
        });

        //Add the scroll pane to this panel.
        add(scrollPane);
    }

    private  class FMPanelModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        //private final FMDirectory currentDir;
        private final List<FMElement> elements;

        public FMPanelModel(FMElementCollection currentDir) {
            //this.currentDir = currentDir;
            this.elements = getElementList(currentDir.content());

        }

        public int getColumnCount() {
            return collumns.size();
        }

        public int getRowCount() {
            return elements.size();
        }

        public String getColumnName(int col) {
            return collumns.get(col).name();
        }

        public Object getValueAt(int row, int col) {
            return collumns.get(col).data(elements.get(row));
        }

        /*
         * JTable uses this method to determine the default renderer/ editor for each
         * cell. If we didn't implement this method, then the last column would contain
         * text ("true"/"false"), rather than a check box.
         */
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's editable.
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
}