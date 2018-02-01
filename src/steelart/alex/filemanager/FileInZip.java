package steelart.alex.filemanager;

import java.util.zip.ZipEntry;

/**
 * File manager element represented file in ZIP archive
 *
 * @author Alexey Merkulov
 * @date 2 February 2018
 */
class FileInZip implements FMElement {
    public final ZipEntry entry;
    public final String name;

    public FileInZip(ZipEntry entry, String name) {
        this.entry = entry;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long size() {
        return entry.getSize();
    }
}
