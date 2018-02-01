package steelart.alex.filemanager.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import steelart.alex.filemanager.FMElementCollection;


/**
 * Main Swing GUI class
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public class SFMWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    public SFMWindow(FMElementCollection start) {
        super("Simple Example");
        this.setBounds(100,100,1000,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        {
            JMenuItem exitItem = new JMenuItem("Exit");
            fileMenu.add(exitItem);
            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);

        //Create and set up the content pane.
        SFMPanel newContentPane = new SFMPanel(start);
        newContentPane.setOpaque(true); //content panes must be opaque
        this.setContentPane(newContentPane);
    }
}
