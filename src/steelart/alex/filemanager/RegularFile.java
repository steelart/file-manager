package steelart.alex.filemanager;

import java.io.File;

/**
 * Regular file element in panel
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public class RegularFile implements FMElement {
    private final File file;
    private final long size;

    public RegularFile(File file) {
        this.file = file;
        this.size = file.length();
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public FileProvider requestFile() {
        return FileProvider.fromFile(file);
    }
}
