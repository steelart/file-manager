package steelart.alex.filemanager;

/**
 * File manager directory element
 *
 * @author Alexey Merkulov
 * @date 27 January 2018
 */
public interface FMDirectory extends FMElement {
    @Override
    public default FMDirectory asDir() {
        return this;
    }

    @Override
    public default long size() {
        //TODO: make a special constant to indicate unknown size
        return -1;
    }

    /** Calculate directory content and enter to it */
    public FMPanel enterDir();
}
