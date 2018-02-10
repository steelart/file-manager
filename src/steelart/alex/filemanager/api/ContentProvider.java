package steelart.alex.filemanager.api;

import java.io.File;
import java.io.IOException;

import steelart.alex.filemanager.FileProvider;

/**
 * Content provider for preview plug-ins.
 * The core idea is to make extendable provider so late
 * it will be possible to add new file type identification methods
 * but preserve old methods for old plug-ins
 *
 * TODO: maybe it is better to merge this type with {@link FileProvider}
 *
 * @author Alexey Merkulov
 * @date 9 February 2018
 */
public interface ContentProvider {
    /** Just get file data representation */
    public File getFile();

    /**
     * Try to provide mime type
     * @return mime type or null if for some reason it could not be provided
     */
    public String getMimeType() throws IOException;
}
