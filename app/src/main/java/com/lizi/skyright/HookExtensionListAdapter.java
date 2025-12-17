package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class HookExtensionListAdapter extends BaseAdapter implements Runnable,CompoundButton.OnCheckedChangeListener {

    private SystemServerManager systemServerManager;
    private Handler handler;
    private Context context;
    private List<ApplicationInfo> hookPackages;
    private PackageManager pm;

    public HookExtensionListAdapter(Context context) {
        this.context = context;
        this.pm = context.getPackageManager();
        this.handler = new Handler();
        this.systemServerManager = SystemServerManager.getManagerInstance();
        this.hookPackages = new ArrayList<>();
        new ThreadLoad().start();
    }

    @Override
    public void run() {
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return hookPackages.size();
    }

    @Override
    public ApplicationInfo getItem(int p) {
        return hookPackages.get(p);
    }

    @Override
    public long getItemId(int p) {
        return p;
    }

    @Override
    public View getView(int p, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        ApplicationInfo app = getItem(p);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.hook_extension_item, null);
            holder.image = convertView.findViewById(R.id.hookextensionitemImageView1);
            holder.appName = convertView.findViewById(R.id.hookextensionitemTextView1);
            holder.packageName = convertView.findViewById(R.id.hookextensionitemTextView2);
            holder.mSwitch = convertView.findViewById(R.id.hookextensionitemSwitch1);
            holder.mSwitch.setOnCheckedChangeListener(this);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
		}
        String packageName = app.packageName;
        holder.image.setImageDrawable(app.loadIcon(pm));
        holder.appName.setText(app.loadLabel(pm));
        holder.packageName.setText(packageName);
        holder.mSwitch.setChecked(systemServerManager.isEnabledHookPackage(packageName));
        holder.mSwitch.setTag(packageName);
        return convertView;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean p) {
        if (systemServerManager.isPauseAllHook()) {
            compoundButton.setChecked(false);
            Toast.makeText(context, "已临时禁用所有模块，扩展模块不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        if (compoundButton.isPressed()) {
            String packageName = compoundButton.getTag().toString();
            systemServerManager.setEnabledHookPackage(packageName, p);
        }
    }

    private static class ViewHolder {
        ImageView image;
        TextView appName;
        TextView packageName;
        Switch mSwitch;
	}

    private class ThreadLoad extends Thread {

        @Override
        public void run() {
            hookPackages.clear();
            List<ApplicationInfo> appList = systemServerManager.getInstalledApplications();
            int size = appList.size();
            for (int i = 0; i < size; i++) {
                ApplicationInfo appInfo = systemServerManager.getApplicationInfo(appList.get(i).packageName, PackageManager.GET_META_DATA);
                if (appInfo.metaData != null) {
                    String hookValue = appInfo.metaData.getString("skyrightHook", null);
                    if (hookValue != null) {
                        hookPackages.add(appInfo);
                    }
                }
            }
            handler.post(HookExtensionListAdapter.this);
        }

    }

}
