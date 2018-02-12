package steelart.alex.filemanager;

import steelart.alex.filemanager.swing.SFMWindow;

/**
 * File manager entry point class
 *
 * @author Alexey Merkulov
 * @date 25 January 2017
 */
public class FMMain {
    public static void main(String[] args) {
        // There could be command line arguments parsing
        SFMWindow app = new SFMWindow(FMUtils.goToPath(""));
        app.setVisible(true);
    }
}
