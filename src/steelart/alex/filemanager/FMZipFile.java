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

    /** Full element path is needed because of windows path difference */
    private final String elementPath;

    public FMZipFile(FMElement element, Supplier<FMElementCollection> exitPoint, String elementPath) {
        this.element = element;
        this.exitPoint = exitPoint;
        this.elementPath = elementPath;
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
    public FMElementCollection enter(ProgressTracker progress) throws IOException, InterruptedException {
        ZipFile zip = null;
        try (FileProvider provider = element.requestFile(progress)) {
            File file = provider.get();
            progress.startPhase("Reading ZIP archive " + name(), false);
            zip = new ZipFile(file);
            DirInZip dir = DirInZip.constructDirTree(zip, exitPoint, elementPath, name(), progress);
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
    public FileProvider requestFile(ProgressTracker progress) throws IOException, InterruptedException {
        return element.requestFile(progress);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}
