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
        SFMWindow app = new SFMWindow(new RegularDirectory(new File(curDirPath)).enter());
        app.setVisible(true);
    }
}
