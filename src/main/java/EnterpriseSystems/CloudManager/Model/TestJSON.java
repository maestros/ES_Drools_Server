package EnterpriseSystems.CloudManager.Model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/03/2013
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class TestJSON {

    public static void main(String[] args) throws JSONException {
//        Blade blade = new Blade((long) 12, 10, 9, 8, 7, 6, true, 4);
//        blade.addVM(new VM((long) 1, 233, 5343, null));
//
//        Gson gson = new Gson();
//        String content = gson.toJson(blade);
//
//        System.out.println(content);
//
//        Blade reanimated = gson.fromJson(content, blade.getClass());
//        System.out.println(reanimated.getDisk_total_GB());
//        System.out.println(reanimated.getVms());
//        System.out.println(reanimated.getVms().get((long) 1).getDiskUsage_GB());


        JSONArray array = new JSONArray();
        JSONObject node = new JSONObject();
        node.put("action", "ShutDownBlade");
        node.put("BladeID", "23");

        array.put(node);
        System.out.println(array.toString());

        String s = array.toString();

        JSONArray newArray = new JSONArray(s);
        JSONObject newObject = newArray.getJSONObject(0);
        System.out.println(newObject);
        System.out.println(newObject.get("action"));

    }
}
