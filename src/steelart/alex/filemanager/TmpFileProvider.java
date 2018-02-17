package steelart.alex.filemanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Temporary file provider
 *
 * TODO: implement cache for same files
 *
 * @author Alexey Merkulov
 * @date 17 February 2018
 */
class TmpFileProvider implements FileProvider {
    private final File tmpFile;
    boolean shouldDelete = true;

    public TmpFileProvider(File tmpFile) {
        this.tmpFile = tmpFile;
    }

    @Override
    public File get() {
        return tmpFile;
    }

    @Override
    public void preserve() {
        shouldDelete = false;

    }

    @Override
    public void close() {
        if (shouldDelete) {
            tmpFile.delete();
        }
    }

    public static FileProvider create(InputStream is, String name, ProgressTracker progress, long size) throws IOException {
        File tmp = null;
        try {
            tmp = File.createTempFile("simple-file-manager-", name);
            tmp.deleteOnExit();
            try (OutputStream output = new FileOutputStream(tmp)) {
                copy(is, output, progress, size);
                FileProvider fileProvider = new TmpFileProvider(tmp);
                tmp = null;
                return fileProvider;
            }
        } finally {
            if (tmp != null)
                tmp.delete();
        }
    }

    private static long copy(InputStream input, OutputStream output, ProgressTracker progress, long size) throws IOException {
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
