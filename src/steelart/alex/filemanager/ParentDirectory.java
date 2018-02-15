package steelart.alex.filemanager;

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

    public ParentDirectory(FMElementCollection cur, Supplier<FMElementCollection> exitPoint) {
        this.cur = cur;
        this.exitPoint = exitPoint;
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
        return exitPoint.get();
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
