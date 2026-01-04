package com.lizi.skyright;
import org.json.JSONObject;

public interface DataUpdateListener {
    
    void dataUpdate(String key,JSONObject data);
    
}
