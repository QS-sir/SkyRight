package com.lizi.skyright;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.content.pm.PackageManager;

public class MonitorActivityListAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

    private ActivityInfo[] activityInfo;
    private Context context;
    private PackageManager pm;

    public MonitorActivityListAdapter(Context context) {
        this.context = context;
        this.activityInfo = new ActivityInfo[0];
        this.pm = context.getPackageManager();
    }


    public void notifyDataSetChanged(ActivityInfo[] activityInfo) {
        if (activityInfo != null) {
            this.activityInfo = activityInfo;
        }else{
            this.activityInfo = new ActivityInfo[0];
        }
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return activityInfo.length;
    }

    @Override
    public ActivityInfo getItem(int p) {
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
       // String className = info.packageName;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.monitor_activity_item, null);
            holder.imageView = view.findViewById(R.id.monitoractivityitemImageView1);
            holder.className = view.findViewById(R.id.monitoractivityitemTextView1);
            holder.label = view.findViewById(R.id.monitoractivityitemTextView2);
            holder.radioButton1 = view.findViewById(R.id.monitoractivityitemRadioButton1);
            holder.radioButton2 = view.findViewById(R.id.monitoractivityitemRadioButton2);
            holder.radioButton3 = view.findViewById(R.id.monitoractivityitemRadioButton3);
            holder.radioButton1.setOnCheckedChangeListener(this);
            holder.radioButton2.setOnCheckedChangeListener(this);
            holder.radioButton3.setOnCheckedChangeListener(this);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
		}
        holder.imageView.setBackground(info.loadIcon(pm));
        holder.className.setText(info.name);
        holder.label.setText(info.loadLabel(pm));
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean p) {

    }


    private static class ViewHolder {
        ImageView imageView;
        TextView className;
        TextView label;
        RadioButton radioButton1;
        RadioButton radioButton2;
        RadioButton radioButton3;
	}

}
