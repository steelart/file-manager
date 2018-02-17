package steelart.alex.filemanager.swing;

import java.awt.Component;

import steelart.alex.filemanager.ProxyProgressTracker;

/**
 * External listener for file manager panel actions
 *
 * @author Alexey Merkulov
 * @date 16 February 2018
 */
public interface FMPanelListener {
    /** Ask about preview specified element */
    public void previewAction(Component preview, String title);

    /** Notify about current directory change */
    public void directoryChangedNotify(String path);

    public void startPossibleLongOperation();
    public void endPossibleLongOperation();
    public void enterWaitMode(ProxyProgressTracker tracker);
}
