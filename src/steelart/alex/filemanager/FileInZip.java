package steelart.alex.filemanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * File manager element represented file in ZIP archive
 *
 * @author Alexey Merkulov
 * @date 2 February 2018
 */
class FileInZip implements FMElement {
    private final ZipFile zip;
    public final ZipEntry entry;
    public final String name;

    public FileInZip(ZipFile zip, ZipEntry entry, String name) {
        this.entry = entry;
        this.name = name;
        this.zip = zip;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long size() {
        return entry.getSize();
    }

    @Override
    public FileProvider requestFile(ProgressTracker progress) throws IOException {
        try (InputStream is = zip.getInputStream(entry)) {
            return TmpFileProvider.create(is, name(), progress, entry.getSize());
        }
    }
}
