package steelart.alex.filemanager.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import steelart.alex.filemanager.ContentProviderImpl;
import steelart.alex.filemanager.FMElement;
import steelart.alex.filemanager.FMElementCollection;
import steelart.alex.filemanager.FMFTPDirectory;
import steelart.alex.filemanager.FileProvider;
import steelart.alex.filemanager.api.ContentProvider;
import steelart.alex.filemanager.api.swing.SwingPreviewPlugin;


/**
 * Main Swing GUI class
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public class SFMWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private final List<SwingPreviewPlugin> plugins = Arrays.asList(new SwingImagePreviewPlugin(), new SwingTextPreviewPlugin());

    private final SFMPanel panel;
    private boolean previewMode = false;

    public SFMWindow(FMElementCollection start) {
        super("Simple Example");
        this.setBounds(100,100,800,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new SFMPanel(this::preview, start);
        panel.setOpaque(true); //content panes must be opaque
        this.setContentPane(panel);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        addExitMenuItem(fileMenu);
        addFtpServerMenuItem(fileMenu);
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);

        addHookForEsc();
    }

    private void preview(FMElement element) {
        try (FileProvider provider = element.requestFile()) {
            if (provider == null)
                return;
            File file = provider.get();
            Component preview = findPreview(file);
            if (preview != null) {
                JPanel previewPanel = new JPanel(new GridLayout(1,0));
                previewPanel.add(preview);
                this.setContentPane(previewPanel);
                previewMode = true;
                revalidate();
                repaint();
                preview.requestFocus();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private Component findPreview(File file) throws IOException {
        ContentProvider provider = new ContentProviderImpl(file);
        for (SwingPreviewPlugin p : plugins) {
            Component preview = p.getPreview(provider);
            if (preview != null)
                return preview;
        }
        System.out.println("No preview for file: " + file);
        System.out.println("Mime type is: " + provider.getMimeType());
        return null;
    }

    private void addExitMenuItem(JMenu fileMenu) {
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void addFtpServerMenuItem(JMenu fileMenu) {
        JMenuItem ftpServer = new JMenuItem("FTP server");
        fileMenu.add(ftpServer);
        ftpServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = (String)JOptionPane.showInputDialog(
                                    SFMWindow.this,
                                    "Choose FTP server",
                                    "Choose FTP server",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    "");

                if (s != null) {
                    FMElementCollection cur = panel.getCurrentDirectory();
                    FMElementCollection directory = FMFTPDirectory.enterFtpServer(s, () -> cur);
                    if (directory != null) {
                        panel.enterNewDir(directory);
                    }
                }
            }
        });
    }

    private void addHookForEsc() {
        //TODO: try to avoid this global hook!
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                int code = e.getKeyCode();
                switch (code) {
                case KeyEvent.VK_ESCAPE:
                    if (previewMode) {
                        SFMWindow.this.setContentPane(panel);
                        previewMode = false;
                        repaint();
                        panel.requestFocus();
                    }
                break;
                }
                return false;
            }
        });
    }
}
