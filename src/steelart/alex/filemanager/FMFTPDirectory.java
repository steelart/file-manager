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
    public FileProvider requestFile() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public FMElementCollection enter() {
        try {
            return constructFromCurFtpDir(path + '/' + name(), client, exitPoint, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static FMElementCollection enterFtpServer(String server, Supplier<FMElementCollection> exitPoint) {
        FTPClient client = new FTPClient();
        try {
            client.connect(server);
            client.enterLocalPassiveMode();
            client.login("anonymous", "");
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (client.isConnected()) {
                return constructFromCurFtpDir("", client, exitPoint, true);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            disconnect(client);
            return null;
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
                return "ftp://" + client.getRemoteAddress().getHostName() + path;
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
}
