package steelart.alex.filemanager;

import java.io.IOException;

/**
 * File manager element in panel (usually it is some file or directory)
 *
 * @author Alexey Merkulov
 * @date 25 January 2018
 */
public interface FMElement {
    /** name of file manager element */
    public String name();
    /** size of file manager element */
    public long size();

    /** @return directory representation of this element or null (for simple files) */
    public default FMEnterable asEnterable() {
        return null;
    }

    /** @param progress TODO
     * @return file provider or null if this element is a directory */
    public FileProvider requestFile(ProgressTracker progress) throws IOException;
}
