package steelart.alex.filemanager.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import steelart.alex.filemanager.FMElementCollection;
import steelart.alex.filemanager.OperationInterrupt;
import steelart.alex.filemanager.ProgressTracker;
import steelart.alex.filemanager.ProxyProgressTracker;


/**
 * Main Swing GUI class
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public class SFMWindow extends JFrame implements FMPanelListener {
    private static final long serialVersionUID = 1L;

    private final SFMPanel panel;
    private boolean previewMode = false;

    /** It becomes true when file manage starts some possible long operation */
    private boolean possibleLongOperationStarted = false;
    private ProxyProgressTracker waitTracker = null;

    public SFMWindow(FMElementCollection start) {
        super(start.path());
        this.setBounds(100,100,800,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new SFMPanel(this, start);
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

    //-------------------------//
    //--State related methods--//
    //-------------------------//

    // For now this method is not needed synchronization
    @Override
    public void directoryChangedNotify(String path) {
        // TODO: Window title crops end of path, but better to crop start!
        // Maybe it will be better to create some status bar
        this.setTitle(path);
    }

    private synchronized void changeProgress(JProgressBar progressBar, long cur, long whole) throws OperationInterrupt {
        int percent = (int)(100*cur/whole);
        progressBar.setValue(percent);
        if (waitTracker == null || waitTracker.isInterrupted()) {
            throw new OperationInterrupt();
        }
    }

    public synchronized void startPossibleLongOperation() {
        possibleLongOperationStarted = true;
    }

    public synchronized void endPossibleLongOperation() {
        possibleLongOperationStarted = false;
        if (waitTracker != null) {
            waitTracker = null;
            restorePanel();
        }
    }

    @Override
    public synchronized void enterWaitMode(ProxyProgressTracker tracker) {
        if (!possibleLongOperationStarted) {
            // operation was fast
            return;
        }
        waitTracker = tracker;

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener((e) -> interruptWaiting());

        JPanel progressPanel = new JPanel();
        progressPanel.add(progressBar);
        progressPanel.add(stopButton);
        this.setContentPane(progressPanel);
        revalidate();
        repaint();

        waitTracker.setTracker(new ProgressTracker() {
            @Override
            public void currentProgress(long cur, long whole) throws OperationInterrupt {
                changeProgress(progressBar, cur, whole);
            }
        });
    }

    @Override
    public synchronized void previewAction(Component preview, String title) {
        possibleLongOperationStarted = false;
        waitTracker = null;

        JPanel previewPanel = new JPanel(new GridLayout(1,0));
        previewPanel.add(preview);
        this.setContentPane(previewPanel);
        this.setTitle(title);
        previewMode = true;
        revalidate();
        repaint();
        preview.requestFocus();
    }

    private synchronized void restorePanel() {
        this.setContentPane(panel);
        previewMode = false;
        this.setTitle(panel.getCurrentDirectory().path());
        repaint();
        panel.requestFocus();
    }

    private synchronized void interruptWaiting() {
        if (waitTracker == null)
            return;
        waitTracker.interrupted();
        endPossibleLongOperation();
    }

    //--------------------------//
    //--Initialization methods--//
    //--------------------------//

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
        JMenuItem ftpServer = new JMenuItem("goto");
        fileMenu.add(ftpServer);
        ftpServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = (String)JOptionPane.showInputDialog(
                                    SFMWindow.this,
                                    "Specify path",
                                    "Specify path",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    panel.getCurrentDirectory().path());

                if (s != null) {
                    panel.resetDir(s);
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
                        restorePanel();
                    }
                    if (waitTracker != null) {
                        interruptWaiting();
                    }
                break;
                }
                return false;
            }

        });
    }
}
