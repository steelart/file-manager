package steelart.alex.filemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.net.ftp.FTPClient;
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
    private final FTPClient client;
    private final Supplier<FMElementCollection> exitPoint;


    private FMFTPDirectory(String path, String name, FTPClient client, Supplier<FMElementCollection> exitPoint) {
        this.path = path;
        this.name = name;
        this.client = client;
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
        return constructFromCurFtpDir(path + '/' + name(), client, exitPoint, false);
    }

    public static FMElementCollection enterFtpServer(String server, Supplier<FMElementCollection> exitPoint) throws IOException {
        FTPClient client = new FTPClient();
        try {
            client.connect(server);
            client.enterLocalPassiveMode();
            client.login("anonymous", "");
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (client.isConnected()) {
                FMElementCollection res = constructFromCurFtpDir("", client, exitPoint, true);
                client = null;
                return res;
            }
            return null;
        } finally {
            if (client != null)
                disconnect(client);
        }
    }

    private static void disconnect(FTPClient client) {
        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FMElementCollection constructFromCurFtpDir(String path, FTPClient client, Supplier<FMElementCollection> exitPoint, boolean disconnectOnExit) throws IOException {
        List<FMElement> content = new ArrayList<>();
        FMElementCollection res = new FMElementCollection() {
            @Override
            public FMElementCollection leaveDir() {
                if (disconnectOnExit) {
                    disconnect(client);
                }
                return exitPoint != null ? exitPoint.get() : null;
            }

            @Override
            public Collection<FMElement> content() {
                return content;
            }

            @Override
            public String path() {
                return ftpPath(path, client);
            }
        };
        FTPFile[] ftpFiles = client.listFiles(path);
        for (FTPFile ftpFile : ftpFiles) {
            // Check if FTPFile is a regular file
            if (ftpFile.getType() == FTPFile.FILE_TYPE) {
                FMFTPFile fmFtpFile = new FMFTPFile(path, ftpFile, client);
                FMElement e = FMUtils.filterElement(fmFtpFile, () -> res, res.path());
                content.add(e);
            }
            if (ftpFile.getType() == FTPFile.DIRECTORY_TYPE) {
                content.add(new FMFTPDirectory(path, ftpFile.getName(), client, () -> res));
            }
        }
        if (exitPoint != null) {
            content.add(new ParentDirectory(res, exitPoint));
        }
        return res;
    }

    public static String ftpPath(String path, FTPClient client) {
        return "ftp://" + client.getRemoteAddress().getHostName() + path;
    }
}
