package steelart.alex.filemanager;

import java.io.File;

/**
 * Regular directory element in panel
 *
 * @author Alexey Merkulov
 * @date 27 January 2018
 */
public class RegularDirectory implements FMDirectory {
    private final File file;

    public RegularDirectory(File file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public FMPanel enterDir() {
        return FMPanel.constructForDirectory(file);
    }
}
