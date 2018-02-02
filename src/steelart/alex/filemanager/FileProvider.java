package steelart.alex.filemanager;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * This interface is needed to request file (real or temporary) for file manager element.
 * This interface implements Closeable, so it could be used in try() auto-closeable construction.
 *
 * @author Alexey Merkulov
 * @date 3 February 2018
 */
public interface FileProvider extends Closeable {
    public File get();

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
        };
    }

    public static FileProvider tmpFileForInputStream(InputStream is, String name) {
        try {
            File tmp = File.createTempFile("simple-file-manager-", name);
            tmp.deleteOnExit();
            try (OutputStream output = new FileOutputStream(tmp)) {
                IOUtils.copy(is, output);
                return new FileProvider() {
                    @Override
                    public File get() {
                        return tmp;
                    }
                    @Override
                    public void close() {
                        tmp.delete();
                    }
                };
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
