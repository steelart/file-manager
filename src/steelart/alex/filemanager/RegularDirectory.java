package steelart.alex.filemanager;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Regular directory element in panel
 *
 * @author Alexey Merkulov
 * @date 27 January 2018
 */
public class RegularDirectory implements FMEnterable {
    private final File dir;

    public RegularDirectory(File file) {
        this.dir = file;
    }

    @Override
    public String name() {
        return dir.getName();
    }

    @Override
    public FMElementCollection enter() {
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
        };
        File p = dir.getParentFile();
        if (p != null)
            elements.add(new ParentDirectory(fmDirectory, () -> new RegularDirectory(dir.getParentFile()).enter()));
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                    elements.add(new FMZipFile(file, () -> this.enter()));
                } else {
                    elements.add(new RegularFile(file));
                }
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
}
