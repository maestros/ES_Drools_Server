package EnterpriseSystems.CloudManager.Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

import EnterpriseSystems.CloudManager.DroolsManagement.DroolsManager;
import EnterpriseSystems.CloudManager.Model.Cloud;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.log4j.Logger;

/**
 * Author: Yi Chen    Date:2013-3-3
 * Description:
 */
public class CloudCommunicator implements Runnable {

    private static final Logger LOG = Logger.getLogger(CloudCommunicator.class.getCanonicalName());
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private DroolsManager droolsManager;
    private boolean alive;

    public CloudCommunicator(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.droolsManager = new DroolsManager(this);
        this.alive = true;

    }

    public void run() {
        String data;
        listenForInput();
        Gson gson = new Gson();
        try {
            while ((data = in.readLine()) != null) {
                if (!data.isEmpty()) {
                    // System.out.println("recieved " + data);

                    //Step 3: Convert data to POJOs
                    Cloud cloud = gson.fromJson(data, Cloud.class);

                    //Step 4: Put the POJO to rule engine.
                    droolsManager.addObjectsToModel(cloud);

                }
                data = null;
            }
        } catch (IOException e) {
            LOG.warn("Socket error.");
            alive = false;

            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e1) {
                    LOG.error("Socket error 2.");
                }
        }

    }


    /**
     * For use with simulator only
     */
    private void listenForInput() {
        Thread consolReader = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader consol = new BufferedReader(new InputStreamReader(System.in));
                try {
                    while (alive) {
                        String command = consol.readLine();
                        System.out.println("sending " + command + " to sim");
                        JSONArray array = new JSONArray();
                        JSONObject node = new JSONObject();
                        node.put("action", "SetScenario");
                        node.put("Scenario", command);
                        array.put(node);
                        System.out.println(array);
                        out.println(array.toString());
                        out.flush();

                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        consolReader.start();
    }

    public void Send2Client(String command) {
        out.println(command);
        out.flush();
    }
}
