package steelart.alex.filemanager;

/**
 * Progress tracker could be used for progress bar
 * and to interrupt long process
 *
 * @author Alexey Merkulov
 * @date 13 February 2018
 */
public interface ProgressTracker {

    /**
     * @param description could be null
     */
    public void startPhase(String description, boolean hasProgress);

    /** This method should be used to inform about current progress */
    public void currentProgress(long cur, long whole) throws InterruptedException;

    public static ProgressTracker empty() {
        return new ProgressTracker() {
            @Override
            public void currentProgress(long cur, long whole) {
            }
            @Override
            public void startPhase(String description, boolean hasProgress) {
            }
        };
    }
}
