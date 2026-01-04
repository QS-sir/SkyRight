package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PackageListAdapter extends BaseAdapter implements Runnable {

    private HideLoadWindow hideLoadWindow;
    private Context context;
    private SystemServerManager systemServerManager;
    private List<ApplicationInfo> applications;
    private PackageManager pm;
    private volatile boolean inSearch; 
    private Handler handler;

    public PackageListAdapter(HideLoadWindow hideLoadWindow) {
        this.hideLoadWindow = hideLoadWindow;
        this.context = hideLoadWindow.getContext();
        this.systemServerManager = SystemServerManager.getManagerInstance();
        this.applications = new ArrayList<>();
        this.pm = context.getPackageManager();
        this.handler = new Handler();
        new ThreadLoad().start();
    }

    public void notifyDataSetChanged(String search) {
        inSearch = false;
        if (search != null && !search.isEmpty()) {
            new ThreadSearch(search).start();
        } else {
            new ThreadLoad().start();
        }
    }
    
    
    @Override
    public void run() {
        notifyDataSetChanged();
        hideLoadWindow.onHideWindow();
    }
    
    private class ThreadLoad extends Thread {

        @Override
        public void run() {
           applications = systemServerManager.getInstalledApplications();
            handler.post(PackageListAdapter.this);
        }
        
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
            String lowerSearch = search.trim().toLowerCase();
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
            handler.post(PackageListAdapter.this);
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
            convertView = View.inflate(context, R.layout.package_item, null);
            holder.image = convertView.findViewById(R.id.packageitemImageView1);
            holder.appName = convertView.findViewById(R.id.packageitemTextView1);
            holder.packageName = convertView.findViewById(R.id.packageitemTextView2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String packageName = app.packageName;
        holder.image.setBackground(ApplicationIcon.getPackageIcon(packageName));
        holder.appName.setText(app.loadLabel(pm));
        holder.packageName.setText(packageName);
        return convertView;
    }


    private static class ViewHolder {
        ImageView image;
        TextView appName;
        TextView packageName;
    }

    public static interface HideLoadWindow{
       void onHideWindow();
       Context getContext();
    }
}
