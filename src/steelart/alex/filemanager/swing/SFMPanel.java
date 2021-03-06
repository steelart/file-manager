package steelart.alex.filemanager.swing;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import steelart.alex.filemanager.FMElementCollection;
import steelart.alex.filemanager.ContentProviderImpl;
import steelart.alex.filemanager.ElementColumnProperty;
import steelart.alex.filemanager.FMUtils;
import steelart.alex.filemanager.FileProvider;
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
    private abstract class WorkerWithExceptionHandling<T, V> extends SwingWorker<T, V> {
        private final ProxySwingProgressTracker tracker;

        WorkerWithExceptionHandling(ProxySwingProgressTracker tracker) {
            this.tracker = tracker;
        }

        protected abstract void internalDone() throws InterruptedException, ExecutionException;

        protected final void done() {
            try {
                internalDone();
            } catch (ExecutionException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(SFMPanel.this,
                        e.getMessage(),
                        "Unexpected problem",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException | CancellationException e) {
                //TODO: should it be done something here?
            } finally {
                listener.endPossibleLongOperation(tracker);
            }
        }
    }

    private static final long serialVersionUID = 1L;

    private final FMPanelListener listener;

    private final List<SwingPreviewPlugin> plugins = Arrays.asList(new SwingImagePreviewPlugin(), new SwingTextPreviewPlugin());

    private final List<ElementColumnProperty> columns = Arrays.asList(ElementColumnProperty.NAME, ElementColumnProperty.SIZE);
    private JTable table;
    private FMElementCollection curDir;
    private List<FMElement> elements;
    private SortKey sortKey = new SortKey(0, SortOrder.ASCENDING);

    public SFMPanel(FMPanelListener listener, FMElementCollection start) {
        super(new GridLayout(1,0));
        this.listener = listener;

        resetTable(start);
    }

    private void resortElements() {
        elements = FMUtils.getSortedList(curDir.content(), columns.get(sortKey.getColumn()), sortKey.getSortOrder() != SortOrder.ASCENDING);
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
        chooseStartElement();
    }

    private void chooseStartElement() {
        String start = curDir.startElementName();
        if (start != null) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).name().equals(start)) {
                    selectElement(i);
                    return;
                }
            }
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

        Rectangle cellRect = table.getCellRect(row, 0, false);
        table.scrollRectToVisible(cellRect);
    }

    private void createTable() {
        FMPanelModel pm = new FMPanelModel();
        table = new JTable(pm);
        table.setRowSorter(new ElementSorter());

        table.setFillsViewportHeight(true);
        table.setCellSelectionEnabled(true);

        ListSelectionListener listener = e -> {
            int[] selectedColumns = table.getSelectedColumns();
            if (selectedColumns.length != table.getColumnCount()) {
                table.setColumnSelectionInterval(0, table.getColumnCount()-1);
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

        performPossibleLongTask((tracker) -> new WorkerWithExceptionHandling<Component, Void>(tracker) {
            @Override
            protected Component doInBackground() throws Exception {
                try (FileProvider provider = element.requestFile(tracker)) {
                    if (provider == null)
                        return null;
                    File file = provider.get();
                    // This could be long operation without possible progress bar...
                    // TODO: implement interruptible preview
                    tracker.startPhase("Preparing preview for " + element.name(), false);
                    return findPreview(file);
                }
            }

            @Override
            protected void internalDone() throws InterruptedException, ExecutionException {
                Component preview = get();
                if (preview != null) {
                    listener.previewAction(preview, element.name());
                }
            }
        });
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
            performPossibleLongTask((tracker) -> new WorkerWithExceptionHandling<FMElementCollection, Void>(tracker) {
                @Override
                protected FMElementCollection doInBackground() throws Exception {
                    return enterable.enter(tracker);
                }
                @Override
                protected void internalDone() throws InterruptedException, ExecutionException {
                    FMElementCollection newDir = get();
                    enterNewDir(newDir);
                }
            });
        } else {
            performPossibleLongTask((tracker) -> new WorkerWithExceptionHandling<Void, Void>(tracker) {
                @Override
                protected Void doInBackground() throws Exception {
                    try (FileProvider provider = element.requestFile(tracker)) {
                        File file = provider.get();
                        Desktop.getDesktop().open(file);
                        provider.preserve();
                    }
                    return null;
                }
                @Override
                protected void internalDone() throws InterruptedException, ExecutionException {
                    get();
                }
            });
        }
    }

    private void performPossibleLongTask(Function<ProxySwingProgressTracker, SwingWorker<?, ?>> workerFunc) {
        ProxySwingProgressTracker tracker = new ProxySwingProgressTracker();
        SwingWorker<?, ?> worker = workerFunc.apply(tracker);
        tracker.setWorker(worker);
        worker.execute();
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
        return elements.get(selected);
    }

    public void resetDir(String s) {
        performPossibleLongTask((tracker) -> new WorkerWithExceptionHandling<FMElementCollection, Void>(tracker) {
            @Override
            protected FMElementCollection doInBackground() throws Exception {
                FMElementCollection directory = FMUtils.goToPath(s, tracker);
                while (curDir != null) curDir = curDir.leaveDir();
                return directory;
            }

            @Override
            protected void internalDone() throws InterruptedException, ExecutionException {
                FMElementCollection directory = get();
                if (directory != null) {
                    enterNewDir(directory);
                }
            }
        });
    }

    private void enterNewDir(FMElementCollection newDir) {
        resetTable(newDir);
        listener.directoryChangedNotify(newDir.path());
        repaint();
    }

    private class FMPanelModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        public int getColumnCount() {
            return columns.size();
        }

        public int getRowCount() {
            return elements.size();
        }

        public String getColumnName(int col) {
            return columns.get(col).propName();
        }

        public Object getValueAt(int row, int col) {
            return columns.get(col).data(elements.get(row));
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