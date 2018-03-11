package steelart.alex.filemanager.swing;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import steelart.alex.filemanager.ProgressTracker;

/**
 * Swing progress-bar tracker implementation
 *
 * @author Alexey Merkulov
 * @date 4 March 2018
 */
class SwingProgressTracker implements ProgressTracker {
    private final JFrame window;
    private final JProgressBar progressBar;
    private final ProxySwingProgressTracker tracker;

    /** Accessed only from worker thread */
    private int prevValue = 0;

    private SwingProgressTracker(JFrame window, JPanel progressPanel, ProxySwingProgressTracker tracker) {
        this.window = window;
        this.tracker = tracker;
        this.progressBar = new JProgressBar();
        progressPanel.add(progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setValue(0);
    }

    // Should be called from background thread!
    @Override
    public void currentProgress(long cur, long whole) throws InterruptedException {
        // TODO: not sure about checking thread interrupted status
        if (tracker.isCanceled() || Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        int percent = (int)(100*cur/whole);
        if (percent != prevValue) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(percent);
                // Progress bar is not smooth without revalidate
                progressBar.revalidate();
            });
        }
        prevValue = percent;
    }

    // Should be called from background thread!
    @Override
    public void startPhase(String description, boolean hasProgress) {
        SwingUtilities.invokeLater(() -> {
            if (description != null) window.setTitle(description);

            progressBar.setIndeterminate(!hasProgress);
            progressBar.setStringPainted(hasProgress);
            progressBar.setValue(0);
            progressBar.repaint();
        });
    }

    public static JPanel createPanel(ProxySwingProgressTracker tracker, JFrame window, ActionListener interrupt) {
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(interrupt);

        JPanel progressPanel = new JPanel();
        progressPanel.add(stopButton);
        tracker.setTracker(new SwingProgressTracker(window, progressPanel, tracker));

        return progressPanel;
    }
}
