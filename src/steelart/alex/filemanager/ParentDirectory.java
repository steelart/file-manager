package steelart.alex.filemanager;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * File manager parent directory exit point
 *
 * @author Alexey Merkulov
 * @date 29 January 2018
 */
public class ParentDirectory implements FMEnterable {
    private final FMElementCollection cur;
    private final Supplier<FMElementCollection> exitPoint;
    private final String elementName;

    public ParentDirectory(FMElementCollection cur, Supplier<FMElementCollection> exitPoint, String elementName) {
        this.cur = cur;
        this.exitPoint = exitPoint;
        this.elementName = elementName;
    }

    @Override
    public String name() {
        return "..";
    }

    @Override
    public long size() {
        return -1;
    }

    @Override
    public FMElementCollection enter(ProgressTracker progress) {
        cur.leaveDir();
        FMElementCollection res = exitPoint.get();
        return wrapper(res, elementName);
    }

    /**
     * The result object should not hold reference to 'cur' directory
     * (it is potential memory leak), so this method is static.
     */
    private static FMElementCollection wrapper(FMElementCollection res, String elementName) {
        return new FMElementCollection() {
            @Override
            public String path() {
                return res.path();
            }

            @Override
            public FMElementCollection leaveDir() {
                return res.leaveDir();
            }

            @Override
            public Collection<FMElement> content() {
                return res.content();
            }

            @Override
            public String startElementName() {
                return elementName;
            }
        };
    }

    @Override
    public FileProvider requestFile(ProgressTracker progress) {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
}
