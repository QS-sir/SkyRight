package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModifyStartActivityAdapter extends BaseAdapter implements View.OnClickListener {

    private ActivityInfo[] activityInfo;
    private Context context;
    private PackageManager pm;
    private Map<String,String> map;
    private SystemServerManager systemServerManager;
    private List<ActivityInfo> searchActivity;
    private boolean isSearch;

    public ModifyStartActivityAdapter(Context context) {
        this.context = context;
        this.activityInfo = new ActivityInfo[0];
        this.pm = context.getPackageManager();
        this.systemServerManager = SystemServerManager.getManagerInstance();
        this.map = JsonParser.getMapStringData(systemServerManager.getStorageData(), SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY);
    }

    @Override
    public void notifyDataSetChanged() {
        map = JsonParser.getMapStringData(systemServerManager.getStorageData(), SystemServerManagerImpl.MODIFY_PACKAGES_START_ACTIVITY);
        super.notifyDataSetChanged();
    }


    public void notifyDataSetChanged(ActivityInfo[] activityInfo) {
        this.activityInfo = activityInfo;
        super.notifyDataSetChanged();
    }

    public void notifyDataSetChanged(String search) {
        if (searchActivity == null) {
            searchActivity = new ArrayList<>();
        } else {
            searchActivity.clear();
        }
        if (search != null && !search.isEmpty()) {
            int length = activityInfo.length;
            for (int i = 0; i < length; i++) {
                ActivityInfo info = activityInfo[i];
                String n = info.name;
                String p = info.packageName;
                String s = search.toLowerCase();
                if (n.toLowerCase().contains(s) || p.toLowerCase().contains(s)) {
                    searchActivity.add(info);
                }
            }
            isSearch = true;
        } else {
            isSearch = false;
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        if (isSearch && searchActivity != null) {
            return searchActivity.size();
        }
        return activityInfo.length;
    }

    @Override
    public ActivityInfo getItem(int p) {
        if (isSearch && searchActivity != null) {
            return searchActivity.get(p);
        }
        return activityInfo[p];
    }

    @Override
    public long getItemId(int p) {
        return p;
    }

    @Override
    public View getView(int p, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        ActivityInfo app = getItem(p);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.start_activity_item, null);
            holder.image = convertView.findViewById(R.id.startactivityitemImageView1);
            holder.name = convertView.findViewById(R.id.startactivityitemTextView1);
            holder.className = convertView.findViewById(R.id.startactivityitemTextView2);
            holder.button = convertView.findViewById(R.id.startactivityitemTextView3);
            holder.button.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.image.setBackground(app.loadIcon(pm));
        holder.name.setText(app.loadLabel(pm));
        holder.className.setText(app.name);
        holder.button.setTag(app);
        return convertView;
    }

    @Override
    public void onClick(View view) {
        ActivityInfo act = (ActivityInfo) view.getTag();
        String pkg = act.packageName;
        String className = act.name;
        String start = systemServerManager.getPackageLaunchActivityName(pkg);
        if (map.containsKey(pkg)) {
            String ac = map.get(pkg);
            if (ac.equals(className)) {
                Toast.makeText(context, "该活动已修改为启动活动无需重复修改", Toast.LENGTH_SHORT).show();
            } else if (className.equals(start)) {
                systemServerManager.setModifyPackageStartActivity(pkg, null);
                Toast.makeText(context, "已恢复默认启动活动", Toast.LENGTH_SHORT).show();
            } else {
                systemServerManager.setModifyPackageStartActivity(pkg, className);
                Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (className.equals(start)) {
                Toast.makeText(context, "该活动为默认启动活动请重新选择", Toast.LENGTH_SHORT).show();
            } else {
                systemServerManager.setModifyPackageStartActivity(pkg, className);
                Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class ViewHolder {
        ImageView image;
        TextView name;
        TextView className;
        TextView button;
    }

}
