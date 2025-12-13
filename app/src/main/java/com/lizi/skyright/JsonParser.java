package com.lizi.skyright;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonParser {

    public static final String TAG = "JsonParser";

    private JsonParser() {
    }
    
    public static List<String> getListData(String data, String key) {
        List<String> list = new ArrayList<>();
        if (data == null || key == null) return list;
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has(key)) {
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    // 防止数组中存在 null 值
                    String item = jsonArray.optString(i, null);
                    if (item != null) {
                        list.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getListData error: " + e.toString());
        }
        return list;
    }

    
    public static Map<String, String> getMapStringData(String data, String key) {
        Map<String, String> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has(key)) {
                JSONObject json = jsonObject.getJSONObject(key);
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    String v = json.optString(k, null);
                    if (v != null) {
                        map.put(k, v);
                    }
                }
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getMapStringData error: " + e.toString());
        }
        return map;
    }

    public static Map<String, List<String>> getMapListData(String data, String key) {
        Map<String, List<String>> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has(key)) {
                JSONObject json = jsonObject.getJSONObject(key);
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    List<String> valueList = new ArrayList<>();
                    JSONArray array = json.optJSONArray(k); // 使用 opt 更安全
                    if (array != null) {
                        int length = array.length();
                        for (int i = 0; i < length; i++) {
                            String item = array.optString(i, null);
                            if (item != null) {
                                valueList.add(item);
                            }
                        }
                    }
                    map.put(k, valueList);
                }
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getMapListData error: " + e.toString());
        }
        return map;
    }

    public static Map<String, Map<String, String>> getMapData(String data, String key) {
        Map<String, Map<String, String>> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has(key)) {
                JSONObject json = jsonObject.getJSONObject(key);
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    JSONObject vObj = json.optJSONObject(k);
                    if (vObj != null) {
                        Map<String, String> innerMap = new HashMap<>();
                        Iterator<String> vKeys = vObj.keys();
                        while (vKeys.hasNext()) {
                            String vk = vKeys.next();
                            String vv = vObj.optString(vk, null);
                            if (vv != null) {
                                innerMap.put(vk, vv);
                            }
                        }
                        map.put(k, innerMap);
                    }
                }
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getMapData error: " + e.toString());
        }
        return map;
    }
}

