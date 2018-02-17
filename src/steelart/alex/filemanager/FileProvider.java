package steelart.alex.filemanager;

import java.io.Closeable;
import java.io.File;

/**
 * This interface is needed to request file (real or temporary) for file manager element.
 * This interface implements Closeable, so it could be used in try() auto-closeable construction.
 *
 * @author Alexey Merkulov
 * @date 3 February 2018
 */
public interface FileProvider extends Closeable {
    public File get();

    /** Preserve file and do not delete it in case of temporary files */
    public void preserve();

    @Override
    public void close();

    public static FileProvider fromFile(File file) {
        return new FileProvider() {
            @Override
            public File get() {
                return file;
            }
            @Override
            public void close() {
            }
            @Override
            public void preserve() {
            }
        };
    }
}
