package steelart.alex.filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
* A collection of static methods for file manager project
*
* @author Alexey Merkulov
* @date 2 February 2018
*/
public class FMUtils {
    private static final String FTP_PROTOCOL_PREFIX = "ftp://";

    // TODO: it could be made as parameter (whether we need to use directory sort or not)
    // NOTE: used reverse e1 and e2:
    private static final Comparator<FMElement> dirComp = (e1, e2) -> Boolean.compare(isDir(e2), isDir(e1));

    // TODO: used a hack with instanceof!
    // NOTE: used reverse e1 and e2:
    private static final Comparator<FMElement> parentDirComp = (e1, e2) -> Boolean.compare(e2 instanceof ParentDirectory, e1 instanceof ParentDirectory);

    public static List<FMElement> getSortedList(Collection<FMElement> elements, ElementColumnProperty property, boolean reversed) {
        List<FMElement> sorted = new ArrayList<>(elements);
        Comparator<FMElement> comparator = property.comparator(reversed);
        sorted.sort(parentDirComp.thenComparing(dirComp).thenComparing(comparator));
        return sorted;
    }

    public static FMElementCollection goToPath(String path, ProgressTracker tracker) throws IOException, InterruptedException {
        if (path.startsWith(FTP_PROTOCOL_PREFIX)) {

            String withoutPrefix = path.substring(FTP_PROTOCOL_PREFIX.length());
            if (withoutPrefix.isEmpty()) {
                throw new IOException("Empty FTP address");
            }

            String[] parts = withoutPrefix.split("/");
            List<String> list = Arrays.asList(parts);
            String hostName = list.get(0);
            List<String> dirPath = list.subList(1, list.size());

            tracker.startPhase("Connecting " + hostName, false);
            FMElementCollection host = FMFTPDirectory.enterFtpServer(hostName, null);
            return goToPath(host, dirPath, tracker);
        } else {
            File dir = new File(path);
            List<String> sufix = new LinkedList<>();
            while (!dir.isDirectory()) {
                sufix.add(0, dir.getName());
                dir = dir.getParentFile();
                if (dir == null) {
                    Path currentRelativePath = Paths.get("");
                    String curDirPath = currentRelativePath.toAbsolutePath().toString();
                    dir = new File(curDirPath);
                    if (path.isEmpty())
                        sufix = Collections.emptyList();
                    break;
                }
            }
            FMEnterable enterable = new RegularDirectory(dir);
            FMElementCollection directory = enterable.enter(tracker);
            return goToPath(directory, sufix, tracker);
        }
    }
    private static FMElementCollection goToPath(FMElementCollection cur, List<String> dirPath, ProgressTracker tracker) throws IOException, InterruptedException {
        if (dirPath.isEmpty())
            return cur;
        String name = dirPath.get(0);
        for (FMElement e : cur.content()) {
            if (e.name().equals(name)) {
                FMEnterable enterable = e.asEnterable();
                if (enterable != null) {
                    FMElementCollection nextDir = enterable.enter(tracker);
                    return goToPath(nextDir, dirPath.subList(1, dirPath.size()), tracker);
                } else {
                    throw new IOException("Could not enter file '" + name + "' at " + cur.path());
                }
            }
        }
        throw new IOException("No file or directory with name '" + name + "' at " + cur.path());
    }

    public static FMElement filterElement(FMElement e, Supplier<FMElementCollection> exitPoint, String elementPath) {
        if (e.name().endsWith(".zip") || e.name().endsWith(".jar")) {
            return new FMZipFile(e, exitPoint, elementPath);
        } else {
            return e;
        }
    }

    /** A shortcut to test {@link FMElement} to be a directory */
    public static boolean isDir(FMElement e) {
        FMEnterable enterable = e.asEnterable();
        if (enterable != null)
            return enterable.isDirectory();
        return false;
    }
}
