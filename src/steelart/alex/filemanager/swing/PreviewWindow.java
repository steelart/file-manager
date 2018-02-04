package steelart.alex.filemanager.swing;

import java.awt.Graphics;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * Preview window stub
 *
 * @author Alexey Merkulov
 * @date 4 February 2018
 */
public class PreviewWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    PreviewWindow() {
        super("Preview");
        this.setBounds(300,300,1000,500);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public synchronized void resetImage(Image image) {
        //removeAll();
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null)
                    g.drawImage(image, 0, 0, null);
            }
        };

        resetPanel(panel);
    }
    public synchronized void resetText(File f) throws IOException {
        FileReader fr = new FileReader(f);
        BufferedReader reader = new BufferedReader(fr);
        JTextArea area = new JTextArea( 16, 58 );
        area.read(reader, "HelloWorld");
        area.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(area);
        JPanel panel = new JPanel();
        panel.add(scrollPane);

        resetPanel(panel);
    }

    private void resetPanel(JPanel panel) {
        panel.setOpaque(false); //content panes must be opaque
        this.setContentPane(panel);
        this.setBounds(300,300,1000,500);
        repaint();
        revalidate();
        repaint();
        setVisible(true);
    }
}
