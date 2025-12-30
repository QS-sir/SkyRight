package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.os.Handler;

public class HideAccessibilityStatusAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener,Runnable {

    private Context context;
    private SystemServerManager systemServerManager;
    private List<ApplicationInfo> applications;
    private Set<String> hideList;
    private PackageManager pm;
    private Handler handler;
    private volatile boolean inSearch;

    public HideAccessibilityStatusAdapter(Context context) {
        this.context = context;
        this.systemServerManager = SystemServerManager.getManagerInstance();
        this.applications = systemServerManager.getInstalledApplications();
        this.hideList = JsonParser.getListData(systemServerManager.getStorageData(), SystemServerManagerImpl.PACKAGES_HIDE_ACCESSIBILITY);
        this.pm = context.getPackageManager();
        this.handler = new Handler();
    }
    
    

    public void notifyDataSetChanged(String search) {
        inSearch = false;
        if (search != null && !search.isEmpty()) {
            new ThreadSearch(search).start();
        } else {
            applications = systemServerManager.getInstalledApplications();
            handler.post(this);
        }
    }

    @Override
    public void run() {
        super.notifyDataSetChanged();
    }
    
    private class ThreadSearch extends Thread {

        private final String search;

        public ThreadSearch(String search) {
            this.search = search;
        }

        @Override
        public void run() {
            inSearch = true;
            List<ApplicationInfo> list = new ArrayList<>();
            String lowerSearch = search.toLowerCase().trim();
            List<ApplicationInfo> appList = systemServerManager.getInstalledApplications();
            for (ApplicationInfo app:appList) {
                if (inSearch) {
                    String appName = app.loadLabel(pm).toString();
                    String packageName = app.packageName;
                    String uid = String.valueOf(app.uid);
                    // 全部转为小写后再判断是否包含
                    if (appName.toLowerCase().contains(lowerSearch) ||
                        packageName.toLowerCase().contains(lowerSearch) ||
                        uid.contains(lowerSearch)) {
                        list.add(app);
                    }
                } else {
                    return;
                }
            }
            applications = list;
            inSearch = false;
            handler.post(HideAccessibilityStatusAdapter.this);
        }

    }

    @Override
    public int getCount() {
        return applications.size();
    }

    @Override
    public ApplicationInfo getItem(int p) {
        return applications.get(p);
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
            convertView = View.inflate(context, R.layout.hide_accessibility_item, null);
            holder.image = convertView.findViewById(R.id.hideaccessibilityitemImageView1);
            holder.appName = convertView.findViewById(R.id.hideaccessibilityitemTextView1);
            holder.packageName = convertView.findViewById(R.id.hideaccessibilityitemTextView2);
            holder.mSwitch = convertView.findViewById(R.id.hideaccessibilityitemSwitch1);
            holder.mSwitch.setOnCheckedChangeListener(this);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String packageName = app.packageName;
        holder.image.setBackground(ApplicationIcon.getPackageIcon(packageName));
        holder.appName.setText(app.loadLabel(pm));
        holder.packageName.setText(packageName);
        holder.mSwitch.setChecked(hideList.contains(packageName));
        holder.mSwitch.setTag(app);
        return convertView;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean p) {
        if (compoundButton.isPressed()) {
            ApplicationInfo app = (ApplicationInfo) compoundButton.getTag();
            if (app.uid > 2000) {
                String packageName = app.packageName;
                systemServerManager.setPackageHideAccessibilityStatus(packageName, p);
                this.hideList = JsonParser.getListData(systemServerManager.getStorageData(),SystemServerManagerImpl.PACKAGES_HIDE_ACCESSIBILITY);
            } else {
                Toast.makeText(context, "核心应用不建议隐藏", Toast.LENGTH_SHORT).show();
                compoundButton.setChecked(false);
            }
        }
    }

    private static class ViewHolder {
        ImageView image;
        TextView appName;
        TextView packageName;
        Switch mSwitch;
    }


}
