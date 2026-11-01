List<ResolveInfo> visibleApps = new ArrayList<>();
for (ResolveInfo info : allApps) {
    if (hiddenAppList.contains(info.activityInfo.packageName)) {
        continue;
    }
    visibleApps.add(info);
}
