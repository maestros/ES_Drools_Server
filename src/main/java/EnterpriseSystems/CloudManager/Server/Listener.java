package EnterpriseSystems.CloudManager.Server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.*;

/**
 * Author: Yi Chen    Date:2013-3-2
 * Description:
 */
public class Listener implements Runnable {

    public static int port = 2103;
    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = null;
    private int POOL_SIZE = 2;

    private static final Logger LOG = Logger.getLogger(Listener.class.getCanonicalName());

    public Listener() {
        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(POOL_SIZE);
            LOG.info("Cloud DroolsManager Server is listening on port " + port);
        } catch (Exception e) {
            LOG.error("Initial socket failed.");
            System.exit(1);
        }
    }

    public void startService() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                threadPool.execute(new CloudCommunicator(socket));
                LOG.info("Accept client: " + socket.getInetAddress().getHostAddress());
            } catch (Exception e) {
                LOG.warn("Accept client failed.");
            }
        }
    }




    public void run() {
        this.startService();
    }

}
