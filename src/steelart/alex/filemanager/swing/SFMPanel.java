package steelart.alex.filemanager.swing;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import steelart.alex.filemanager.FMElementCollection;
import steelart.alex.filemanager.ElementColumnProperty;
import steelart.alex.filemanager.FMUtils;
import steelart.alex.filemanager.FileProvider;
import steelart.alex.filemanager.ProgressTracker;
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

    private final FMPanelListener listener;

    private final List<ElementColumnProperty> collumns = Arrays.asList(ElementColumnProperty.NAME, ElementColumnProperty.SIZE);
    private volatile JTable table;
    private volatile FMElementCollection curDir;
    private volatile List<FMElement> elements;

    public SFMPanel(FMPanelListener listener, FMElementCollection start) {
        super(new GridLayout(1,0));
        this.listener = listener;

        resetTable(start);
    }

    private static List<FMElement> getElementList(Collection<FMElement> elements) {
        return FMUtils.getSortedList(elements, ElementColumnProperty.NAME, false);
    }

    public FMElementCollection getCurrentDirectory() {
        return curDir;
    }

    @Override
    public void requestFocus() {
        if (table == null) {
            super.requestFocus();
        } else {
            table.requestFocus();
        }
    }

    private void resetTable(FMElementCollection newDir) {
        curDir = newDir;
        this.elements = getElementList(curDir.content());
        if (table == null) {
            createTable();
        } else {
            table.revalidate();
        }
        selectFirstElement();
    }

    private void selectFirstElement() {
        selectElement(0);
    }

    private void selectLastElement() {
        selectElement(table.getRowCount()-1);
    }

    private void selectElement(int row) {
        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(0, table.getColumnCount()-1);
    }

    private void createTable() {
        FMPanelModel pm = new FMPanelModel();
        table = new JTable(pm);

        table.setFillsViewportHeight(true);
        table.setCellSelectionEnabled(true);

        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int[] selectedColumns = table.getSelectedColumns();
                if (selectedColumns.length != table.getColumnCount()) {
                    table.setColumnSelectionInterval(0, table.getColumnCount()-1);
                }
            }
        };

        // For some magic reason it is needed to hook selection mode for column model
        // Selection mode hook for whole table is not enough
        //table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        table.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                switch (code) {
                case KeyEvent.VK_F3:
                    previewAction();
                    break;
                case KeyEvent.VK_ENTER:
                    enterAction();
                    break;
                case KeyEvent.VK_HOME:
                    selectFirstElement();
                    break;
                case KeyEvent.VK_END:
                    selectLastElement();
                    break;
                }
            }
        });

        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount() >= 2){
                    enterAction();
                }
            }
        });

        //TODO: Here is used implemented specific behavior from DefaultTableCellRenderer...
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setFont(Font font) {
                // Original getTableCellRendererComponent implementation uses setFont by value from table.getFont
                // So here we disable that action!
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                FMElement e = elements.get(row);
                Font f = table.getFont();
                if (FMUtils.isDir(e)) {
                    f = f.deriveFont(Font.BOLD);
                } else if (e.asEnterable() != null) {
                    f = f.deriveFont(Font.ITALIC);
                }
                super.setFont(f);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        this.add(new JScrollPane(table));
    }

    private void previewAction() {
        FMElement element = getCurElement();
        if (element != null)
            listener.previewAction(element);
    }

    private void enterAction() {
        FMElement element = getCurElement();
        if (element == null) return;
        FMEnterable enterable = element.asEnterable();
        if (enterable != null) {
            enterDirAction(enterable);
        } else {
            try (FileProvider provider = element.requestFile(ProgressTracker.empty())) {
                File file = provider.get();
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enterDirAction(FMEnterable enterable) {
        try {
            enterNewDir(enterable.enter(ProgressTracker.empty()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FMElement getCurElement() {
        int[] selectedRow = table.getSelectedRows();
        if (selectedRow == null || selectedRow.length == 0) {
            return null;
        }
        int selected = selectedRow[0];
        FMElement element = elements.get(selected);
        return element;
    }

    public void resetDir(String s) {
        FMElementCollection directory = null;
        try {
            directory = FMUtils.goToPath(s, ProgressTracker.empty());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (directory == null) {
            return;
        }
        while (curDir != null) curDir = curDir.leaveDir();
        enterNewDir(directory);
    }

    private void enterNewDir(FMElementCollection newDir) {
        resetTable(newDir);
        listener.directoryChangedNotify(newDir);
        repaint();
    }

    private  class FMPanelModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        public FMPanelModel() {
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