package steelart.alex.filemanager;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPFile;

/**
 * It is a file manager element represented FTP file
 *
 * @author Alexey Merkulov
 * @date 4 February 2018
 */
public class FMFTPFile implements FMElement {
    private final String path;
    private final FTPFile ftpFile;
    private final FTPConnection connection;


    public FMFTPFile(String path, FTPFile ftpFile, FTPConnection connection) {
        this.path = path;
        this.ftpFile = ftpFile;
        this.connection = connection;
    }

    @Override
    public String name() {
        return ftpFile.getName();
    }

    @Override
    public long size() {
        return ftpFile.getSize();
    }

    @Override
    public FileProvider requestFile(ProgressTracker progress) throws IOException, InterruptedException {
        progress.startPhase("Downloading " + name(), true);
        String fullPath = path + '/' + name();
        FileProvider res = null;
        try (InputStream is = connection.retrieveFileStream(fullPath)) {
            res = TmpFileProvider.create(is, name(), progress, ftpFile.getSize());
            return res;
        } finally {
            boolean completePendingCommand = connection.completePendingCommand();
            if (res != null && !completePendingCommand) {
                // This exception could be thrown only in normal function exit way
                throw new IOException("Could not extract file from " + connection.ftpPath(fullPath));
            }
        }
    }
}