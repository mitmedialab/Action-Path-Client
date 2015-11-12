package org.actionpath.db;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rahulb on 9/30/15.
 */
public class RequestType {

    public static int INVALID_ID = -1;

    public int id;
    public String name;
    public String nickname;


    public RequestType(){

    }

    public static RequestType fromJSONObject(JSONObject info) throws JSONException {
        RequestType rt = new RequestType();
        rt.id = info.getInt("id");
        rt.name = info.getString("name");
        rt.nickname = info.getString("nickname");
        return rt;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id",id);
        obj.put("name",name);
        obj.put("nickname",nickname);
        return obj;
    }

    @Override
    public String toString(){
        return "["+id+"] "+name+" (aka '"+nickname+"')";
    }

}
