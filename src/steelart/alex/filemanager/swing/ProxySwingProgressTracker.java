package steelart.alex.filemanager.swing;

import javax.swing.SwingWorker;

import steelart.alex.filemanager.ProgressTracker;

/**
 * This class could be used for delayed progress bar!
 *
 * @author Alexey Merkulov
 * @date 16 February 2018
 */
class ProxySwingProgressTracker implements ProgressTracker {
    private SwingWorker<?, ?> worker;

    /**
     * To avoid possible deadlock this proxy is not calling
     * tracker methods under synchronized block
     */
    private volatile ProgressTracker tracker = null;

    // Delayed data. They should be accessed under synchronization
    private String lastDescription = null;
    private boolean hasProgress = false;

    /** Should be called only once before worker started */
    public void setWorker(SwingWorker<?, ?> worker) {
        this.worker = worker;
    }

    public boolean isCanceled() {
        return worker.isCancelled();
    }

    /**
     * GUI thread should initialize tracker only once so
     * we could use this variable as volatile
     * <p/>
     * There is some probability that {@link #startPhase(String, boolean)} and
     * {@link #setTracker(ProgressTracker)} could be called in the same time.
     * So we need to synchronize delayed data.
     */
    public void setTracker(ProgressTracker tracker) {
        String lastDescription;
        boolean hasProgress;

        synchronized (this) {
            if (this.tracker != null)
                throw new IllegalStateException("Tracker should be initialized only once");
            this.tracker = tracker;
            lastDescription = this.lastDescription;
            hasProgress = this.hasProgress;
        }

        this.tracker.startPhase(lastDescription, hasProgress);
    }

    /** Changing progress is not very important task, so it could be leave without additional synchronization */
    @Override
    public void currentProgress(long cur, long whole) throws InterruptedException {
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
    public void startPhase(String description, boolean hasProgress) {
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

    public void cancel() {
        worker.cancel(true);
    }

    public boolean isDone() {
        return worker.isDone();
    }
}
