package steelart.alex.filemanager.swing;

import steelart.alex.filemanager.FMElement;
import steelart.alex.filemanager.FMElementCollection;

/**
 * External listener for file manager panel actions
 *
 * @author Alexey Merkulov
 * @date 16 February 2018
 */
public interface FMPanelListener {
    /** Ask about preview specified element */
    public void previewAction(FMElement e);

    /** Notify about current directory change */
    public void directoryChangedNotify(FMElementCollection dir);
}
