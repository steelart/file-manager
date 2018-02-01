package steelart.alex.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.zip.ZipFile;

/**
 * A file manager element wrapper to represent ZIP file.
 * It is enterable element.
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public class FMZipFile implements FMEnterable {
    private final File file;
    private final Supplier<FMElementCollection> exitPoint;

    public FMZipFile(File file, Supplier<FMElementCollection> exitPoint) {
        this.file = file;
        this.exitPoint = exitPoint;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public long size() {
        return file.length();
    }

    @Override
    public FMElementCollection enter() {
        ZipFile zip;
        try {
            zip = new ZipFile(file);
            DirInZip dir = DirInZip.constructDirTree(zip, exitPoint);
            return dir.enter();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Some problem in zip file");
        }
    }
}
