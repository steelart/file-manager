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
    private final FMElement element;
    private final Supplier<FMElementCollection> exitPoint;
    private final String parentPath;

    public FMZipFile(FMElement element, Supplier<FMElementCollection> exitPoint, String parentPath) {
        this.element = element;
        this.exitPoint = exitPoint;
        this.parentPath = parentPath;
    }

    @Override
    public String name() {
        return element.name();
    }

    @Override
    public long size() {
        return element.size();
    }

    @Override
    public FMElementCollection enter(ProgressTracker progress) throws IOException {
        ZipFile zip = null;
        try (FileProvider profider = element.requestFile(progress)) {
            File file = profider.get();
            zip = new ZipFile(file);
            DirInZip dir = DirInZip.constructDirTree(zip, exitPoint, parentPath + '/' + name(), progress);
            FMElementCollection res = dir.simpleEnter();
            zip = null;
            return res;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public FileProvider requestFile(ProgressTracker progress) throws IOException {
        return element.requestFile(progress);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}
