package com.lizi.skyright;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Set;
import java.util.HashSet;

public final class JsonParser {

    public static final String TAG = "JsonParser";

    private JsonParser() {
    }
    
    public static Set<String> getListData(String data, String key) {
        Set<String> list = new HashSet<>();
        if (data == null || key == null) return list;
        try {
            JSONObject jsonObject = new JSONObject(data);
            list = getListData(jsonObject.getJSONArray(key).toString());
        } catch (JSONException e) {
            LogManager.log(TAG, "getListData1 error: " + e.toString());
        }
        return list;
    }
    
    public static Set<String> getListData(JSONObject data, String key) {
        Set<String> list = new HashSet<>();
        if (data == null || key == null) return list;
        try {
            list = getListData(data.getJSONArray(key).toString());
        } catch (JSONException e) {
            LogManager.log(TAG, "getListData1 error: " + e.toString());
        }
        return list;
    }
    
    public static Set<String> getListData(String data){
        Set<String> list = new HashSet<>();
        if (data == null) return list;
        try {
            JSONArray jsonArray = new JSONArray(data);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                // 防止数组中存在 null 值
                String item = jsonArray.optString(i);
                list.add(item);
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getListData2 error: " + e.toString());
        }
        return list;
    }
    
    public static Set<String> getListData(JSONArray jsonArray){
        Set<String> list = new HashSet<>();
        if (jsonArray == null) return list;
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            // 防止数组中存在 null 值
            list.add(jsonArray.optString(i));
        }
        return list;
    }
    
    
    public static Map<String, String> getMapStringData(JSONObject data, String key) {
        Map<String, String> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = data;
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
    
    
    
    public static Map<String, String> getMapStringData(JSONObject json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            String v = json.optString(k,null);
            if (v != null) {
                map.put(k, v);
            }
        }
        return map;
    }
    
    
    public static Map<String, Set<String>> getMapSet(JSONObject data, String key) {
        Map<String, Set<String>> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = data;
            if (jsonObject.has(key)) {
                JSONObject json = jsonObject.getJSONObject(key);
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    Set<String> valueList = new HashSet<>();
                    JSONArray array = json.optJSONArray(k); 
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

    public static Map<String, Set<String>> getMapSet(String data, String key) {
        Map<String, Set<String>> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has(key)) {
                JSONObject json = jsonObject.getJSONObject(key);
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    Set<String> valueList = new HashSet<>();
                    JSONArray array = json.optJSONArray(k); 
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
    
    public static Map<String, Map<String, String>> getMapData(JSONObject json) {
        Map<String, Map<String, String>> map = new HashMap<>();
        if (json == null) return map;
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
        return map;
    }

    public static Map<String, Map<String, String>> getMapData(String data, String key) {
        Map<String, Map<String, String>> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has(key)) {
                JSONObject json = jsonObject.getJSONObject(key);
                map = getMapData(json);
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getMapData error: " + e.toString());
        }
        return map;
    }
    
    public static Map<String, Map<String, String>> getMapData(JSONObject data, String key) {
        Map<String, Map<String, String>> map = new HashMap<>();
        if (data == null || key == null) return map;
        try {
            JSONObject jsonObject = data;
            if (jsonObject.has(key)) {
                JSONObject json = jsonObject.getJSONObject(key);
                map = getMapData(json);
            }
        } catch (JSONException e) {
            LogManager.log(TAG, "getMapData error: " + e.toString());
        }
        return map;
    }
    
}

