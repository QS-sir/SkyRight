package com.lizi.skyright;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class HookExtensionActivity extends Activity {

    private ListView listView;
    private HookExtensionListAdapter hookExtensionListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hook_extension_activity);
        init();
    }

    private void init() {
        SystemServerManager systemServerManager = SystemServerManager.getManagerInstance();
        if (!systemServerManager.isInitService()) {
            return;
        }
        listView = findViewById(R.id.hookextensionactivityListView1);
        hookExtensionListAdapter = new HookExtensionListAdapter(this);
        listView.setAdapter(hookExtensionListAdapter);
    }

}
