package steelart.alex.filemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * This class is a wrapper over FTPClient and it should solve next problems:
 * <ul>
 * <li> Keeping connection alive </li>
 * <li> Reconnection (TODO now) </li>
 * </ul>
 *
 * @author Alexey Merkulov
 * @date 23 February 2018
 */
class FTPConnection {
    private final FTPClient client;
    private volatile boolean disconnected = false;

    private FTPConnection(FTPClient client) {
        this.client = client;
    }

    public FTPFile[] listFiles(String path) throws IOException {
        return client.listFiles(path);
    }

    public String ftpPath(String path) {
        return "ftp://" + client.getRemoteAddress().getHostName() + path;
    }

    public InputStream retrieveFileStream(String fullPath) throws IOException {
        return client.retrieveFileStream(fullPath);
    }

    public boolean completePendingCommand() throws IOException {
        return client.completePendingCommand();
    }

    public void disconnect() {
        disconnected = true;
        disconnect(client);
    }

    private static void disconnect(FTPClient client) {
        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void keepConnection() {
        while(client.isAvailable()) {
            try {
                //10 seconds ping
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (disconnected)
                break;
            try {
                client.noop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static FTPConnection connect(String server) throws SocketException, IOException {
        FTPClient client = new FTPClient();
        try {
            client.connect(server);
            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = client.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                throw new IOException("FTP server " + server + " refused connection.");
            }
            client.enterLocalPassiveMode();// It seems, it works faster with bad Internet connection
            client.login("anonymous", "");
            client.setFileType(FTPClient.BINARY_FILE_TYPE);

            if (!client.isConnected()) {
                throw new IOException("FTP server is not connected for some reason!");
            }

            FTPConnection ftpConnection = new FTPConnection(client);
            client = null;

            new Thread(ftpConnection::keepConnection).start();
            return ftpConnection;
        } finally {
            if (client != null)
                disconnect(client);
        }
    }
}
