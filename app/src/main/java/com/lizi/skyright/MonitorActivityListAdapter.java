package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class MonitorActivityListAdapter extends BaseAdapter implements RadioGroup.OnCheckedChangeListener {

    private ActivityInfo[] activityInfo;
    private Context context;
    private PackageManager pm;
    private SystemServerManager systemServerManager;
    private Map<String,Map<String,String>> map;
    private List<ActivityInfo> searchActivity;
    private boolean isSearch;

    public MonitorActivityListAdapter(Context context) {
        this.context = context;
        this.activityInfo = new ActivityInfo[0];
        this.pm = context.getPackageManager();
        this.systemServerManager = SystemServerManager.getManagerInstance();
        this.map = JsonParser.getMapData(systemServerManager.getStorageData(), SystemServerManagerImpl.MONITOR_ACTIVITYS);
    }


    public void notifyDataSetChanged(ActivityInfo[] activityInfo) {
        if (activityInfo != null) {
            this.activityInfo = activityInfo;
        } else {
            this.activityInfo = new ActivityInfo[0];
        }
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged(String search) {
        if(searchActivity == null){
            searchActivity = new ArrayList<>();
        }else{
            searchActivity.clear();
        }
        if (search != null && !search.isEmpty()) {
            int length = activityInfo.length;
            for (int i = 0; i < length; i++) {
                ActivityInfo info = activityInfo[i];
                String n = info.name;
                String p = info.packageName;
                String s = search.toLowerCase();
                if(n.toLowerCase().contains(s)||p.toLowerCase().contains(s)){
                    searchActivity.add(info);
                }
            }
            isSearch = true;
        } else {
            isSearch = false;
        }
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        this.map = JsonParser.getMapData(systemServerManager.getStorageData(), SystemServerManagerImpl.MONITOR_ACTIVITYS);
        super.notifyDataSetChanged();
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
    public View getView(int p, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        ActivityInfo info = getItem(p);
        String packageName = info.packageName;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.monitor_activity_item, null);
            holder.imageView = view.findViewById(R.id.monitoractivityitemImageView1);
            holder.className = view.findViewById(R.id.monitoractivityitemTextView1);
            holder.label = view.findViewById(R.id.monitoractivityitemTextView2);
            holder.radioGroup = view.findViewById(R.id.monitoractivityitemRadioGroup1);
            holder.radioGroup.setOnCheckedChangeListener(this);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
		}
        String activityName = info.name;
        holder.imageView.setBackground(info.loadIcon(pm));
        holder.className.setText(activityName);
        holder.label.setText(info.loadLabel(pm));
        holder.radioGroup.setTag(info);
        if (!map.containsKey(packageName)) {
            holder.radioGroup.check(R.id.monitoractivityitemRadioButton1);
        } else {
            Map<String,String> m = map.get(packageName);
            if (m.containsKey(activityName)) {
                String aciton = m.get(activityName);
                if (aciton.equals(ActivityRequestDialog.REQUEST_ASK)) {
                    holder.radioGroup.check(R.id.monitoractivityitemRadioButton2);
                } else if (aciton.equals(ActivityRequestDialog.REQUEST_ALWAYS_REFUSE)) {
                    holder.radioGroup.check(R.id.monitoractivityitemRadioButton3);
                } else {
                    holder.radioGroup.check(R.id.monitoractivityitemRadioButton1);
                }
            } else {
                holder.radioGroup.check(R.id.monitoractivityitemRadioButton1);
            }
        }
        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int p) {
        RadioButton radioButton = radioGroup.findViewById(p);
        ActivityInfo info = (ActivityInfo) radioGroup.getTag();
        String act = info.name;
        String pkg = info.packageName;
        if (radioButton.isPressed()) {
            switch (p) {
                case R.id.monitoractivityitemRadioButton1:
                    systemServerManager.setMonitorActivity(pkg, act, ActivityRequestDialog.REQUEST_IGNORE);
                    break;
                case R.id.monitoractivityitemRadioButton2:
                    systemServerManager.setMonitorActivity(pkg, act, ActivityRequestDialog.REQUEST_ASK);
                    break;
                case R.id.monitoractivityitemRadioButton3:
                    systemServerManager.setMonitorActivity(pkg, act, ActivityRequestDialog.REQUEST_ALWAYS_REFUSE);
                    break;
            }
        }
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView className;
        TextView label;
        RadioGroup radioGroup;
	}

}
