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
    public FileProvider requestFile() {
        try {
            InputStream is = client.retrieveFileStream(path + '/' + name());
            FileProvider res = FileProvider.tmpFileForInputStream(is, name());
            if (!client.completePendingCommand()) {
                return null;
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}