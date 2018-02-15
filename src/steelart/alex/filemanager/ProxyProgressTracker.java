package steelart.alex.filemanager;

/**
 * This class could be used for delayed progress bar!
 *
 * @author Alexey Merkulov
 * @date 16 February 2018
 */
public class ProxyProgressTracker implements ProgressTracker {
    private volatile ProgressTracker tracker = null;
    private volatile boolean isInterrupted = false;

    public void setTracker(ProgressTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void currentProgress(long cur, long whole) throws OperationInterrupt {
        if (tracker != null) {
            tracker.currentProgress(cur, whole);
        }
    }

    public void interrupted() {
        isInterrupted = true;
    }

    public boolean isInterrupted() {
        return isInterrupted;
    }
}
