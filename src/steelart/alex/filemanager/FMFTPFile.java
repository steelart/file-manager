package steelart.alex.filemanager;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
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
    private final FTPClient client;


    public FMFTPFile(String path, FTPFile ftpFile, FTPClient client) {
        this.path = path;
        this.ftpFile = ftpFile;
        this.client = client;
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
    public FileProvider requestFile(ProgressTracker progress) throws IOException {
        progress.startPhase("Downloading " + name(), true);
        String fullPath = path + '/' + name();
        FileProvider res = null;
        try (InputStream is = client.retrieveFileStream(fullPath)) {
            res = TmpFileProvider.create(is, name(), progress, ftpFile.getSize());
            return res;
        } finally {
            boolean completePendingCommand = client.completePendingCommand();
            if (res != null && !completePendingCommand) {
                throw new IOException("Could not extract file from " + FMFTPDirectory.ftpPath(fullPath, client));
            }
        }
    }
}