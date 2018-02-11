package steelart.alex.filemanager;

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

    /** Calculate directory content and enter to it */
    public FMElementCollection enter();

    /** @return true iff this element is a directory */
    public boolean isDirectory();
}
