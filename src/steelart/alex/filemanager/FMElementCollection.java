package steelart.alex.filemanager;

import java.util.Collection;

/**
 * This class represents a collection of files.
 * This collection could be associated with some resource
 * (opened ZIP file of FTP connection)
 *
 * @author Alexey Merkulov
 * @date 2 February 2018
 */
public interface FMElementCollection {
    /**
     * Leave current directory and release associated resources (if any)
     *
     * @return parent directory
     */
    public FMElementCollection leaveDir();

    /** @return content of current directory */
    public Collection<FMElement> content();

    /** @return string path to this directory */
    public String path();

    /** @return name of initially selected element or null */
    public default String startElementName() {
        return null;
    }
}
