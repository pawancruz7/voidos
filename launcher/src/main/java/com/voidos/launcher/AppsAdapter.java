package com.voidos.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppsAdapter extends BaseAdapter {

    private Context context;
    private List<ResolveInfo> appsList;
    private PackageManager packageManager;
    private Set<String> hiddenApps;

    public AppsAdapter(Context context, List<ResolveInfo> appsList, PackageManager packageManager) {
        this.context = context;
        this.appsList = appsList;
        this.packageManager = packageManager;
        
        // Hidden apps data read karna
        SharedPreferences prefs = context.getSharedPreferences("VoidOS_Privacy", Context.MODE_PRIVATE);
        this.hiddenApps = prefs.getStringSet("hidden_packages", new HashSet<String>());
    }

    @Override
    public int getCount() {
        return appsList.size();
    }

    @Override
    public Object getItem(int position) {
        return appsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        }

        ImageView appIcon = convertView.findViewById(R.id.app_icon);
        TextView appName = convertView.findViewById(R.id.app_name);

        final ResolveInfo info = appsList.get(position);
        final String packageName = info.activityInfo.packageName;

        // --- PRIVACY HIDE FILTER ---
        // Agar app hidden list me hai, toh item view ko empty (invisible) kar do
        if (hiddenApps.contains(packageName)) {
            convertView.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = convertView.getLayoutParams();
            if (params != null) {
                params.height = 0; // Space crunch avoid karne ke liye height zero kar di
                convertView.setLayoutParams(params);
            }
            return convertView;
        } else {
            convertView.setVisibility(View.VISIBLE);
        }

        appName.setText(info.loadLabel(packageManager));
        appIcon.setImageDrawable(info.loadIcon(packageManager));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    context.startActivity(launchIntent);
                }
            }
        });

        return convertView;
    }
}
