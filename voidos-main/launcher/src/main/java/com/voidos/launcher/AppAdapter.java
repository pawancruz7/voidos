package com.voidos.launcher;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class AppAdapter extends BaseAdapter {

    private final Context context;
    private final List<AppModel> appsList;
    private final LayoutInflater inflater;

    public AppAdapter(Context context, List<AppModel> appsList) {
        this.context = context;
        this.appsList = appsList;
        this.inflater = LayoutInflater.from(context);
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

    private static class ViewHolder {
        ImageView appIcon;
        TextView appLabel;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LinearLayout cellLayout = new LinearLayout(context);
            cellLayout.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            cellLayout.setOrientation(LinearLayout.VERTICAL);
            cellLayout.setGravity(Gravity.CENTER);
            cellLayout.setPadding(0, 16, 0, 16);

            ImageView iconView = new ImageView(context);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(128, 128));
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            cellLayout.addView(iconView);

            TextView labelView = new TextView(context);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0, 12, 0, 0);
            labelView.setLayoutParams(textParams);
            labelView.setTextColor(0xFFE0E0E0);
            labelView.setTextSize(11f);
            labelView.setSingleLine(true);
            labelView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            labelView.setGravity(Gravity.CENTER);
            cellLayout.addView(labelView);

            convertView = cellLayout;

            holder = new ViewHolder();
            holder.appIcon = iconView;
            holder.appLabel = labelView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppModel app = appsList.get(position);
        holder.appIcon.setImageDrawable(app.getIcon());
        holder.appLabel.setText(app.getLabel());

        return convertView;
    }
}
