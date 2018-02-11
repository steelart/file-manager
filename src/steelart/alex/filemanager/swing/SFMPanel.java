package steelart.alex.filemanager.swing;

import java.awt.Component;
import java.awt.Desktop;
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
    private static final long serialVersionUID = 1L;

    private final List<SwingPreviewPlugin> plugins = Arrays.asList(new SwingTextPreviewPlugin(), new SwingImagePreviewPlugin());

    private final PreviewWindow preview = new PreviewWindow();

    private final List<ElementColumnProperty> collumns = Arrays.asList(ElementColumnProperty.NAME, ElementColumnProperty.SIZE);
    private JTable table;
    private FMElementCollection curDir;
    private List<FMElement> elements;

    public SFMPanel(FMElementCollection start) {
        super(new GridLayout(1,0));

        resetTable(start);
    }

    private static List<FMElement> getElementList(Collection<FMElement> elements) {
        return FMUtils.getSortedList(elements, ElementColumnProperty.NAME, false);
    }

    public FMElementCollection getCurrentDirectory() {
        return curDir;
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

        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane);
    }

    private void previewAction() {
        FMElement element = getCurElement();
        if (element == null) return;
        try (FileProvider provider = element.requestFile()) {
            File file = provider.get();
            Component c = findPreview(file);
            if (c != null) {
                preview.resetPanel(c);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void enterAction() {
        FMElement element = getCurElement();
        if (element == null) return;
        FMEnterable enterable = element.asEnterable();
        if (enterable != null) {
            enterNewDir(enterable.enter());
        } else {
            try (FileProvider provider = element.requestFile()) {
                File file = provider.get();
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    private Component findPreview(File file) throws IOException {
        ContentProvider provider = new ContentProviderImpl(file);
        for (SwingPreviewPlugin p : plugins) {
            Component preview = p.getPreview(provider);
            if (preview != null)
                return preview;
        }
        System.out.println("No preview for file: " + file);
        return null;
    }

    public void enterNewDir(FMElementCollection newDir) {
        resetTable(newDir);
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