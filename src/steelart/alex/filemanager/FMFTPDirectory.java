package steelart.alex.filemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.net.ftp.FTPFile;

/**
 * It is a file manager element represented FTP directory
 *
 * @author Alexey Merkulov
 * @date 4 February 2018
 */
public class FMFTPDirectory implements FMEnterable  {
    private final String path;
    private final String name;
    private final FTPConnection connection;
    private final Supplier<FMElementCollection> exitPoint;


    private FMFTPDirectory(String path, String name, FTPConnection connection, Supplier<FMElementCollection> exitPoint) {
        this.path = path;
        this.name = name;
        this.connection = connection;
        this.exitPoint = exitPoint;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long size() {
        return -1;
    }

    @Override
    public FileProvider requestFile(ProgressTracker progress) {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public FMElementCollection enter(ProgressTracker progress) throws IOException {
        return constructFromCurFtpDir(path + '/' + name(), connection, exitPoint, false);
    }

    public static FMElementCollection enterFtpServer(String server, Supplier<FMElementCollection> exitPoint) throws IOException {
        FTPConnection connection = null;
        try {
            connection = FTPConnection.connect(server);
            FMElementCollection res = constructFromCurFtpDir("", connection, exitPoint, true);
            connection = null;
            return res;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private static FMElementCollection constructFromCurFtpDir(String path, FTPConnection connection, Supplier<FMElementCollection> exitPoint, boolean disconnectOnExit) throws IOException {
        List<FMElement> content = new ArrayList<>();
        FMElementCollection res = new FMElementCollection() {
            @Override
            public FMElementCollection leaveDir() {
                if (disconnectOnExit) {
                    connection.disconnect();
                }
                return exitPoint != null ? exitPoint.get() : null;
            }

            @Override
            public Collection<FMElement> content() {
                return content;
            }

            @Override
            public String path() {
                return connection.ftpPath(path);
            }
        };
        FTPFile[] ftpFiles = connection.listFiles(path);
        for (FTPFile ftpFile : ftpFiles) {
            // Check if FTPFile is a regular file
            if (ftpFile.getType() == FTPFile.FILE_TYPE) {
                FMFTPFile fmFtpFile = new FMFTPFile(path, ftpFile, connection);
                FMElement e = FMUtils.filterElement(fmFtpFile, () -> res, res.path());
                content.add(e);
            }
            if (ftpFile.getType() == FTPFile.DIRECTORY_TYPE) {
                content.add(new FMFTPDirectory(path, ftpFile.getName(), connection, () -> res));
            }
        }
        if (exitPoint != null) {
            content.add(new ParentDirectory(res, exitPoint));
        }
        return res;
    }
}
