package steelart.alex.filemanager;

/**
 * This class could be used for delayed progress bar!
 *
 * @author Alexey Merkulov
 * @date 16 February 2018
 */
public class ProxyProgressTracker implements ProgressTracker {
    /**
     * To avoid possible deadlock this proxy is not calling
     * tracker methods under synchronized block
     */
    private volatile ProgressTracker tracker = null;
    private volatile boolean isInterrupted = false;

    private String lastDescription = null;
    private boolean hasProgress;

    /**
     * GUI thread should initialize tracker only once so
     * we could use this variable as volatile
     * <p/>
     * There is some probability that {@link #startPhase(String, boolean)} and
     * {@link #setTracker(ProgressTracker)} could be called in the same time.
     * So we need to synchronize delayed data.
     */
    public synchronized void setTracker(ProgressTracker tracker) {
        String lastDescription = null;
        boolean hasProgress = false;

        synchronized (this) {
            if (this.tracker != null)
                throw new IllegalStateException("Tracker should be initialized only once");
            this.tracker = tracker;
            lastDescription = this.lastDescription;
            hasProgress = this.hasProgress;
        }

        if (lastDescription != null) {
            this.tracker.startPhase(lastDescription, hasProgress);
        }
    }

    @Override
    public void currentProgress(long cur, long whole) throws OperationInterrupt {
        if (tracker != null) {
            tracker.currentProgress(cur, whole);
        }
    }

    /**
     * There is some probability that {@link #startPhase(String, boolean)} and
     * {@link #setTracker(ProgressTracker)} could be called in the same time.
     * So we need to synchronize delayed data.
     */
    @Override
    public synchronized void startPhase(String description, boolean hasProgress) {
        boolean trackerInited = false;
        synchronized (this) {
            if (tracker != null) {
                trackerInited = true;
            } else {
                this.lastDescription = description;
                this.hasProgress = hasProgress;
            }
        }
        if (trackerInited)
            tracker.startPhase(description, hasProgress);
    }

    public void interrupted() {
        isInterrupted = true;
    }

    public boolean isInterrupted() {
        return isInterrupted;
    }
}
