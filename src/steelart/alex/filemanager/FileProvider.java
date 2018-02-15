package steelart.alex.filemanager;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    public static FileProvider tmpFileForInputStream(InputStream is, String name, ProgressTracker progress, long size) throws IOException {
        File tmp = null;
        try {
            File tmpFile = tmp = File.createTempFile("simple-file-manager-", name);
            tmp.deleteOnExit();
            try (OutputStream output = new FileOutputStream(tmp)) {
                copy(is, output, progress, size);
                FileProvider fileProvider = new FileProvider() {
                    @Override
                    public File get() {
                        return tmpFile;
                    }
                    @Override
                    public void close() {
                        tmpFile.delete();
                    }
                };
                tmp = null;
                return fileProvider;
            }
        } finally {
            if (tmp != null)
                tmp.delete();
        }
    }

    public static long copy(InputStream input, OutputStream output, ProgressTracker progress, long size) throws IOException {
        final int DEFAULT_BUFFER_SIZE = 1024 * 4;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            progress.currentProgress(count, size);
        }
        return count;
    }
}
