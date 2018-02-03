package steelart.alex.filemanager;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path currentRelativePath = Paths.get("");
        String curDirPath = currentRelativePath.toAbsolutePath().toString();
        FMEnterable enterable = new RegularDirectory(new File(curDirPath));
        FMElementCollection directory = enterable.enter();
        SFMWindow app = new SFMWindow(directory);
        app.setVisible(true);
    }
}
