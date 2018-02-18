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
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import steelart.alex.filemanager.FMElementCollection;
import steelart.alex.filemanager.ContentProviderImpl;
import steelart.alex.filemanager.ElementColumnProperty;
import steelart.alex.filemanager.FMUtils;
import steelart.alex.filemanager.FileProvider;
import steelart.alex.filemanager.OperationInterrupt;
import steelart.alex.filemanager.ProgressTracker;
import steelart.alex.filemanager.ProxyProgressTracker;
import steelart.alex.filemanager.api.ContentProvider;
import steelart.alex.filemanager.api.swing.SwingPreviewPlugin;
import steelart.alex.filemanager.FMElement;
import steelart.alex.filemanager.FMEnterable;

/**
 * Swing panel implementation
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public class SFMPanel extends JPanel {
    @FunctionalInterface
    private static interface PossibleLongTask {
        void apply(ProxyProgressTracker tracker) throws IOException;
    }

    private static final long serialVersionUID = 1L;

    private final FMPanelListener listener;

    private final List<SwingPreviewPlugin> plugins = Arrays.asList(new SwingImagePreviewPlugin(), new SwingTextPreviewPlugin());

    private final List<ElementColumnProperty> collumns = Arrays.asList(ElementColumnProperty.NAME, ElementColumnProperty.SIZE);
    private volatile JTable table;
    private volatile FMElementCollection curDir;
    private volatile List<FMElement> elements;
    private SortKey sortKey = new SortKey(0, SortOrder.ASCENDING);

    public SFMPanel(FMPanelListener listener, FMElementCollection start) {
        super(new GridLayout(1,0));
        this.listener = listener;

        resetTable(start);
    }

    private void resortElements() {
        elements = FMUtils.getSortedList(curDir.content(), collumns.get(sortKey.getColumn()), sortKey.getSortOrder() != SortOrder.ASCENDING);
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
        resortElements();
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
        table.setRowSorter(new ElementSorter());

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
        if (element == null)
            return;
        performPossibleLongTask((tracker) -> previewAction(element, tracker));
    }

    private void previewAction(FMElement element, ProxyProgressTracker tracker) throws IOException {
        try (FileProvider provider = element.requestFile(tracker)) {
            if (provider == null)
                return;
            File file = provider.get();
            // This could be long operation without possible progress bar...
            // TODO: implement interruptible preview
            tracker.startPhase("Prepearing preview for " + element.name(), false);
            Component preview = findPreview(file);
            // So lets check the process was not interrupted
            // If it was - just ignore preview result for now...
            if (preview != null && !tracker.isInterrupted()) {
                listener.previewAction(preview, element.name());
            }
        }
    }

    private Component findPreview(File file) throws IOException {
        ContentProvider provider = new ContentProviderImpl(file);
        for (SwingPreviewPlugin p : plugins) {
            Component preview = p.getPreview(provider);
            if (preview != null)
                return preview;
        }
        return null;
    }


    private void enterAction() {
        FMElement element = getCurElement();
        if (element == null) return;
        FMEnterable enterable = element.asEnterable();
        if (enterable != null) {
            performPossibleLongTask((tracker) -> enterNewDir(enterable.enter(tracker)));
        } else {
            performPossibleLongTask((tracker) -> openWithStandardProgram(element, tracker));
        }
    }

    private void openWithStandardProgram(FMElement element, ProgressTracker tracker) throws IOException {
        try (FileProvider provider = element.requestFile(tracker)) {
            File file = provider.get();
            Desktop.getDesktop().open(file);
            provider.preserve();
        }
    }

    private void performPossibleLongTask(PossibleLongTask task) {
        ProxyProgressTracker tracker = new ProxyProgressTracker();
        listener.startPossibleLongOperation();
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    task.apply(tracker);
                } catch (OperationInterrupt e) {
                    // just ignore it
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(SFMPanel.this,
                            e.getMessage(),
                            "Unexpected problem",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (!tracker.isInterrupted())
                        listener.endPossibleLongOperation();
                }
            }
        };
        new Thread(runnable).start();
        // TODO: add signal to wake up for fast operations!
        try {
            //give time for fast operations
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listener.enterWaitMode(tracker);
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
        performPossibleLongTask((tracker) -> resetDir(s, tracker));
    }

    private void resetDir(String s, ProgressTracker tracker) throws IOException {
        FMElementCollection directory = FMUtils.goToPath(s, tracker);
        if (directory == null) {
            return;
        }
        while (curDir != null) curDir = curDir.leaveDir();
        enterNewDir(directory);
    }

    private void enterNewDir(FMElementCollection newDir) {
        resetTable(newDir);
        listener.directoryChangedNotify(newDir.path());
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

        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /** It is a not normal sorter: it changes table model instead of just sort it */
    private final class ElementSorter extends RowSorter<TableModel> {
        @Override
        public TableModel getModel() {
            return table.getModel();
        }

        @Override
        public void toggleSortOrder(int column) {
            if (sortKey.getColumn() == column) {
                SortOrder order = sortKey.getSortOrder() == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING;
                sortKey = new SortKey(column, order);
            } else {
                sortKey = new SortKey(column, SortOrder.ASCENDING);
            }
            resortElements();
            System.out.println("Toggle column: " + column);
        }

        @Override
        public int convertRowIndexToModel(int index) {
            return index;
        }

        @Override
        public int convertRowIndexToView(int index) {
            return index;
        }

        @Override
        public List<? extends SortKey> getSortKeys() {
            return Collections.singletonList(sortKey);
        }

        @Override
        public int getViewRowCount() {
            return table.getModel().getRowCount();
        }

        @Override
        public int getModelRowCount() {
            return table.getModel().getRowCount();
        }

        @Override
        public void setSortKeys(List<? extends SortKey> keys) {
            throw new IllegalStateException("allRowsChanged");
        }

        @Override
        public void modelStructureChanged() {
            throw new IllegalStateException("modelStructureChanged");
        }

        @Override
        public void allRowsChanged() {
            throw new IllegalStateException("allRowsChanged");
        }

        @Override
        public void rowsInserted(int firstRow, int endRow) {
            throw new IllegalStateException("rowsInserted: " + firstRow + ":" + endRow);
        }

        @Override
        public void rowsDeleted(int firstRow, int endRow) {
            throw new IllegalStateException("rowsDeleted: " + firstRow + ":" + endRow);
        }

        @Override
        public void rowsUpdated(int firstRow, int endRow) {
            throw new IllegalStateException("rowsUpdated: " + firstRow + ":" + endRow);
        }

        @Override
        public void rowsUpdated(int firstRow, int endRow, int column) {
            throw new IllegalStateException("rowsUpdated: " + firstRow + ":" + endRow + " -> " + column);
        }
    }
}