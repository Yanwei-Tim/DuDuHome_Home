package com.dudu.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.RoutePara;
import com.dudu.agedmodel.R;
import com.dudu.event.GaoMapEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/24.
 */
public class AgedUtils {
    private static File mapFileDir = new File(AgedContacts.AGEDMODEL_APK_DIR, AgedContacts.VMAP_NAME);
    private static File routeFileDir = new File(AgedContacts.AGEDMODEL_APK_DIR, AgedContacts.ROUTE_NAME);
    private static File gaoMapFileDir = new File(FileUtils.getExternalStorageDirectory(), AgedContacts.DETAIL_VMAP_NAME);
    private static File gaoRouteFileDir = new File(FileUtils.getExternalStorageDirectory(), AgedContacts.DETAIL_ROUTE_NAME);
    private static File gaoApkFile = new File(AgedContacts.AGEDMODEL_APK_DIR, AgedContacts.GAO_DE_APK);

    public static void loadOffLine(final Context context) {
        if (mapFileDir.exists() && routeFileDir.exists()) {
            if (FileUtils.isSdCard()) {
                if (!gaoMapFileDir.exists()) {
                    gaoRouteFileDir.mkdirs();
                }
                if (!gaoRouteFileDir.exists()) {
                    gaoRouteFileDir.mkdirs();
                }
                DialogUtils.showCopyMessage(context, context.getString(R.string.loading_map));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.copyFolder(mapFileDir.getPath(), gaoMapFileDir.getPath());
                        FileUtils.copyFolder(routeFileDir.getPath(), gaoRouteFileDir.getPath());
                        Activity activity = (Activity) context;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.dismissCopyMessage();
                            }
                        });
                    }
                }).start();

            }
        }

    }

    public static void openMap(Context context) {
        RoutePara routePara = new RoutePara();

        routePara.setStartName("起点");

        routePara.setEndName("终点");

        routePara.setStartPoint(new LatLng(22.629852, 113.864891));

        routePara.setEndPoint(new LatLng(39.92458861111111, 116.43543861111111));

        routePara.setDrivingRouteStyle(1);

        try {

            AMapUtils.openAMapDrivingRoute(routePara, context);

        } catch (AMapException e) {

            e.printStackTrace();

        }

    }

    public static void installGaoDeMap(Context context) {
        Log.v("jjj","installGaoDeMap");
        if (isAppInstalled(context, AgedContacts.GAO_DE_PACKAGE_NAME)) {
            Log.v("jjjj", "openMap..");
            openMap(context);
        } else {
            if (gaoApkFile.exists()) {

                installApp(context, gaoApkFile);
                waitAndStart(context, AgedContacts.GAO_DE_PACKAGE_NAME);
            }

        }

    }

    private static void waitAndStart(final Context context, final String packageName) {
        new Thread(){
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (isAppInstalled(context, packageName)) {
                        EventBus.getDefault().post(AgedNaviEvent.FloatButtonEvent.SHOW);
                        startThirdPartyApp(context, "com.autonavi.minimap");
                        break;
                    }
                }
            }
        }.start();


    }

    public static void startThirdPartyApp(Context context, String packageName) {
        Intent intent;
        PackageManager packageManager = context.getPackageManager();
        intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    private static void installApp(Context context, File file) {
        EventBus.getDefault().post(new GaoMapEvent(context.getString(R.string.gao_map_installing)));
        Intent intent = new Intent("android.intent.action.VIEW.HIDE");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse(AgedContacts.FILE_NAME + file.toString()), AgedContacts.APPLICATION_NAME);
        context.startActivity(intent);
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    public static boolean checkGaoMaoStall(Context context) {
        return isAppInstalled(context, "com.autonavi.minimap");
    }

    public static void uninstallGaoApk(final Context context) {
        if (isAppInstalled(context, AgedContacts.GAO_DE_PACKAGE_NAME)) {
            EventBus.getDefault().post(new GaoMapEvent(context.getString(R.string.gao_map_uninstalling)));
            Intent intent = new Intent();
            intent.setAction("android.intent.action.DELETE.HIDE");
//            intent.setAction(Intent.ACTION_DELETE);
            intent.setData(Uri.parse(AgedContacts.GAO_AKP_PACKAGE));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            EventBus.getDefault().post(new GaoMapEvent(context.getString(R.string.gao_map_uninstalled)));

        } else {
            DialogUtils.showCopyMessage(context, context.getString(R.string.deleting_map));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileUtils.deleteFile2(gaoMapFileDir);
                    FileUtils.deleteFile2(gaoRouteFileDir);
                    Activity activity = (Activity) context;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.dismissCopyMessage();
                        }
                    });
                }
            }).start();
        }
    }
}
