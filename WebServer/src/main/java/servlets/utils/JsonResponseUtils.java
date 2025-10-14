package servlets.utils;

import com.google.gson.JsonObject;

public class JsonResponseUtils {

    public static JsonObject success(String message){
        JsonObject obj=new JsonObject();
        obj.addProperty("state",ResponseState.SUCCESS.toString());
        obj.addProperty("message",message);
        return obj;
    }


    public static JsonObject error(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("state", ResponseState.ERROR.toString());
        json.addProperty("message", message);
        return json;
    }
}
