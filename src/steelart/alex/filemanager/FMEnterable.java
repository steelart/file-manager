package steelart.alex.filemanager;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A file manager element in which it is possible to enter.
 * Usually it is some directory.
 * But it could be archive also.
 *
 * @author Alexey Merkulov
 * @date 25 January 2018
 */
public interface FMEnterable extends FMElement {
    public default FMEnterable asEnterable() {
        return this;
    }

    /** Calculate directory content and enter to it
     * @param progress TODO*/
    public FMElementCollection enter(ProgressTracker progress) throws IOException, InterruptedException;

    /** @return true iff this element is a directory */
    public boolean isDirectory();

    public default FileProvider requestFile(ProgressTracker progress) throws IOException, InterruptedException {
        if (isDirectory()) {
            throw new FileNotFoundException(name() + " (Is a directory)");
        } else {
            throw new IllegalStateException("Unimplemented requestFile() method for non-directory element");
        }
    }
}
