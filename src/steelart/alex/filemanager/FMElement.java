package steelart.alex.filemanager;


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

    /**
     * Just helper method. Do not redefine it!
     * @return true iff this element is an enterable
     */
    public default boolean isEnterable() {
        return asEnterable() != null;
    }
}
