package EnterpriseSystems.CloudManager.Server;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 05/03/2013
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class Main {


    private static final Logger LOG = Logger.getLogger(Listener.class.getCanonicalName());

    public static void main(String[] args) {
        // Start the listener thread
        if(args.length > 0){
            Listener.port = Integer.parseInt(args[0]);
        }else{
            LOG.warn("You have not set the listening port. 9070 is set as default.");
            LOG.warn("To set the listening port. Use: java Server <port number>");
        }
        new Thread(new Listener()).start();
    }


}
