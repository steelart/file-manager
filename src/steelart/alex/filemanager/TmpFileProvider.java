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
    private boolean shouldDelete = true;

    private TmpFileProvider(File tmpFile) {
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
            deleteFile(tmpFile);
        }
    }

    public static FileProvider create(InputStream is, String name, ProgressTracker progress, long size) throws IOException, InterruptedException {
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
            if (tmp != null) {
                deleteFile(tmp);
            }
        }
    }

    private static void deleteFile(File tmp) {
        boolean success = tmp.delete();
        if (!success) {
            System.err.println("Could not delete temporary file " + tmp);
        }
    }

    private static void copy(InputStream input, OutputStream output, ProgressTracker progress, long size) throws IOException, InterruptedException {
        final int DEFAULT_BUFFER_SIZE = 1024 * 4;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            progress.currentProgress(count, size);
        }
        if (count != size) {
            throw new IOException("Downloaded " + count + " bytes but expected " + size + " bytes");
        }
    }
}
