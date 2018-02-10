package steelart.alex.filemanager.swing;

import java.awt.Component;

import javax.swing.JFrame;


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
        this.setBounds(300,300,500,300);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void resetPanel(Component panel) {
        this.getContentPane().removeAll();
        this.getContentPane().add(panel);
        revalidate();
        repaint();
        setVisible(true);
    }
}
