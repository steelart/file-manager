package steelart.alex.filemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import steelart.alex.filemanager.util.Recursive;

/**
 * File manager element directory in ZIP archive
 *
 * @author Alexey Merkulov
 * @date 2 February 2018
 */
public class DirInZip implements FMEnterable {
    private final Map<String, FMElement> elements = new HashMap<>();

    private final Supplier<FMElementCollection> exitPoint;
    private final String path;
    private final String name;
    // needs to close ZIP archive on exit
    private final ZipFile zip;

    private DirInZip(Supplier<FMElementCollection> exitPoint, String path, String name, ZipFile zip) {
        this.exitPoint = exitPoint;
        this.path = path;
        this.name = name;
        this.zip = zip;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long size() {
        return -1;
    }

    @Override
    public FMElementCollection enter(ProgressTracker progress) {
        return simpleEnter();
    }

    public FMElementCollection simpleEnter() {
        ArrayList<FMElement> res = new ArrayList<>(elements.values());
        FMElementCollection r = new FMElementCollection() {
            @Override
            public Collection<FMElement> content() {
                return res;
            }
            @Override
            public FMElementCollection leaveDir() {
                if (zip != null) {
                    try {
                        zip.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return exitPoint.get();
            }
            @Override
            public String path() {
                return path;
            }
        };
        res.add(new ParentDirectory(r, exitPoint, name()));
        return r;
    }

    private static String constructPath(List<String> path) {
        StringBuilder buf = new StringBuilder();
        for(String part : path) {
            buf.append('/');
            buf.append(part);
        }
        return buf.toString();
    }

    public static DirInZip constructDirTree(
            final ZipFile zip,
            Supplier<FMElementCollection> exitPoint,
            String parentPath,
            String parentName,
            ProgressTracker progress) throws InterruptedException {
        Enumeration<? extends ZipEntry> entries = zip.entries();

        DirInZip root = new DirInZip(exitPoint, parentPath, parentName, zip);

        Recursive<Function<List<String>, DirInZip>> r = new Recursive<>();
        r.func = (List<String> path) -> {
            if (path.isEmpty())
                return root;
            int last = path.size() - 1;
            DirInZip prev = r.func.apply(path.subList(0, last));
            String name = path.get(last);
            String pathStr = parentPath + constructPath(path);
            FMElement res = prev.elements.computeIfAbsent(name, k -> new DirInZip(() -> prev.simpleEnter(), pathStr, name, null));
            if (res instanceof DirInZip)
                return (DirInZip)res;
            else
                throw new IllegalStateException("Incorrect zip file, TODO message");
        };
        Function<List<String>, DirInZip> pathToDir = r.func;
        int size = zip.size();
        int curNum = 0;
        progress.startPhase(null, true);
        while (entries.hasMoreElements()) {
            progress.currentProgress(curNum, size);
            ZipEntry elem = entries.nextElement();
            if (elem.isDirectory())
                continue;
            String[] parts = elem.getName().split("/");
            List<String> list = Arrays.asList(parts);
            int last = list.size() - 1;
            List<String> dirPath = list.subList(0, last);
            String name = list.get(last);
            DirInZip dir = pathToDir.apply(dirPath);
            FileInZip z = new FileInZip(zip, elem, name);
            FMElement e = FMUtils.filterElement(z, () -> dir.simpleEnter(), parentPath + constructPath(dirPath) + '/' + z.name());
            dir.elements.put(name, e);
            curNum++;
        }
        return root;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
}
