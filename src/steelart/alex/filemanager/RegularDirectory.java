package steelart.alex.filemanager;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Regular directory element in panel
 *
 * @author Alexey Merkulov
 * @date 27 January 2018
 */
public class RegularDirectory implements FMEnterable {
    private final File dir;

    public RegularDirectory(File file) {
        this.dir = file.getAbsoluteFile();
    }

    @Override
    public String name() {
        return dir.getName();
    }

    @Override
    public FMElementCollection enter(ProgressTracker progress) {
        Set<FMElement> elements = new HashSet<FMElement>();
        FMElementCollection fmDirectory = new FMElementCollection() {
            @Override
            public Collection<FMElement> content() {
                return elements;
            }
            @Override
            public FMElementCollection leaveDir() {
                return null;
            }
            @Override
            public String path() {
                return dir.getPath();
            }
        };
        File p = dir.getParentFile();
        if (p != null) {
            Supplier<FMElementCollection> exitPoint = () -> new RegularDirectory(dir.getParentFile()).enter(ProgressTracker.empty());
            ParentDirectory parentDir = new ParentDirectory(fmDirectory, exitPoint, name());
            elements.add(parentDir);
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                RegularFile rf = new RegularFile(file);
                FMElement e = FMUtils.filterElement(rf, () -> this.enter(ProgressTracker.empty()), dir.getPath());
                elements.add(e);
            } else if (file.isDirectory()) {
                elements.add(new RegularDirectory(file));
            }
        }
        return fmDirectory;
    }

    @Override
    public long size() {
        return -1;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
}
