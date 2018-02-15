package steelart.alex.filemanager;

import java.io.IOException;

import steelart.alex.filemanager.swing.SFMWindow;

/**
 * File manager entry point class
 *
 * @author Alexey Merkulov
 * @date 25 January 2017
 */
public class FMMain {
    public static void main(String[] args) throws IOException {
        // There could be command line arguments parsing
        SFMWindow app = new SFMWindow(FMUtils.goToPath("", ProgressTracker.empty()));
        app.setVisible(true);
    }
}
