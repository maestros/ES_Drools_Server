package EnterpriseSystems.CloudManager.Model;

/**
 * Decisions Builder
 *
 * 2013/3/3
 * @author Zijun Chen
 */


import EnterpriseSystems.CloudManager.DroolsManagement.DroolsManager;
import EnterpriseSystems.CloudManager.Server.CloudCommunicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DecisionBuilder {

    private CloudCommunicator cloudCommunicator;
    private JSONArray commandToClient;

    public DecisionBuilder(CloudCommunicator cloudCommunicator) {
        this.commandToClient = new JSONArray();
        this.cloudCommunicator = cloudCommunicator;
    }

    public void sendToClient() {
        cloudCommunicator.Send2Client(commandToClient.toString());
        System.out.println(commandToClient.toString());
        this.commandToClient = new JSONArray();
    }


    public void shutdownBlade(Blade blade) {
        try {
            JSONObject node = new JSONObject();
            node.put("action", Decisions.ShutdownBlade);
            node.put("BladeID", blade.getID());
            commandToClient.put(node);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void tryOpenNewBlade() {
        try {
            JSONObject node = new JSONObject();
            node.put("action",Decisions.OpenNewBlade);
            commandToClient.put(node);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void moveVm2Blade(VM vm, Blade toBlade, Blade fromBlade) {
        try {
            JSONObject node = new JSONObject();
            node.put("action", Decisions.Move);
            node.put("VmID", vm.getID());
            node.put("FromBladeID", fromBlade.getID());
            node.put("ToBladeID", toBlade.getID());
            commandToClient.put(node);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCommandToClient(JSONArray commandToClient) {
        this.commandToClient = commandToClient;
    }

    public JSONArray getCommandToClient() {
        return commandToClient;
    }

    public void clearDecisions() {
        this.commandToClient = new JSONArray();
    }
}
