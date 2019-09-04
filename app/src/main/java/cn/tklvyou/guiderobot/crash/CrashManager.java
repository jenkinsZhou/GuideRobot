
package cn.tklvyou.guiderobot.crash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;



import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.tklvyou.guiderobot.base.MyApplication;
import cn.tklvyou.guiderobot.crash.imp.ISave;
import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot_new.R;


/**
 * @author :zhoujian
 * @description : 异常管理
 * @company :途酷科技
 * @date: 2017年07月04日下午 01:36
 * @Email: 971613168@qq.com
 */
@SuppressLint("NewApi")
public final class CrashManager {
    private static final String NEW_LINE = "\r\n";
    private static final String EXTRA_RESTART_ACTIVITY_CLASS = "EXTRA_RESTART_ACTIVITY_CLASS";
    private static final String EXTRA_SHOW_ERROR_DETAILS = "EXTRA_SHOW_ERROR_DETAILS";
    private static final String EXTRA_STACK_TRACE = "EXTRA_STACK_TRACE";
    private static final String EXTRA_IMAGE_DRAWABLE_ID = "EXTRA_IMAGE_DRAWABLE_ID";

    private final static String TAG = "CrashManager";
    private static final String INTENT_ACTION_ERROR_ACTIVITY = "ERROR";
    private static final String INTENT_ACTION_RESTART_ACTIVITY = "RESTART";
    private static final String CAOC_HANDLER_PACKAGE_NAME = "com.crashmanager";
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";
    private static final int MAX_STACK_TRACE_SIZE = 262143;
    private static Application application;
    private static WeakReference<Activity> lastActivityCreated = new WeakReference<>(null);
    private static boolean isInBackground = false;

    private static boolean launchErrorActivityWhenInBackground = true;
    private static boolean showErrorDetails = true;
    private static boolean enableAppRestart = true;
    private static int defaultErrorActivityDrawableId = R.drawable.error_image;
    private static Class<? extends Activity> errorActivityClass = null;
    private static Class<? extends Activity> restartActivityClass = null;
    private static ISave iSave;

    public void setISave(ISave save) {
        CrashManager.iSave = save;
    }

    private static CrashManager instance;

    public static synchronized CrashManager getCrashManagerInstance() {
        if (instance == null) {
            instance = new CrashManager();
        }

        return instance;
    }


    public static void init(Context context) {
        try {
            if (context == null) {
                TourCooLogUtil.e("context为null");
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    TourCooLogUtil.e("API版本过低");
                }
                //INSTALL!
                Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

                if (oldHandler != null && oldHandler.getClass().getName().startsWith(CAOC_HANDLER_PACKAGE_NAME)) {
                    TourCooLogUtil.e("已经处理异常");
                } else {
                    if (oldHandler != null && !oldHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                        TourCooLogUtil.e("已经处理异常");
                    }

                    application = (Application) context.getApplicationContext();
                    //We define a default exception handler that does what we want so it can be called from Crashlytics/ACRA
                    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread, final Throwable throwable) {
//                            Log.e(TAG, "App已经崩溃，执行crashmanager的UncaughtExceptionHandler", throwable);
                            Log.e(TAG, "App已经崩溃，执行crashmanager的UncaughtExceptionHandler--->"+application.toString(), throwable);
                            if (errorActivityClass == null) {
                                errorActivityClass = guessErrorActivityClass(application);
                            }
                            if (isStackTraceLikelyConflictive(throwable, errorActivityClass)) {
                                Log.e(TAG, "您的应用程序类或您的错误活动已崩溃，自定义活动将不会启动！");
                            } else {
                                if (launchErrorActivityWhenInBackground || !isInBackground) {
                                    final Intent intent = new Intent(application, errorActivityClass);
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    throwable.printStackTrace(pw);
                                    String stackTraceString = sw.toString();
                                    if (stackTraceString.length() > MAX_STACK_TRACE_SIZE) {
                                        String disclaimer = " [stack trace 太大]";
                                        stackTraceString = stackTraceString.substring(0, MAX_STACK_TRACE_SIZE - disclaimer.length()) + disclaimer;
                                    }
                                    if (enableAppRestart && restartActivityClass == null) {
                                        restartActivityClass = guessRestartActivityClass(application);
                                    } else if (!enableAppRestart) {
                                        restartActivityClass = null;
                                    }
                                    TourCooLogUtil.e(TAG, "异常原因：" + stackTraceString);
                                    intent.putExtra(EXTRA_STACK_TRACE, stackTraceString);
                                    intent.putExtra(EXTRA_RESTART_ACTIVITY_CLASS, restartActivityClass);
                                    intent.putExtra(EXTRA_SHOW_ERROR_DETAILS, showErrorDetails);
                                    intent.putExtra(EXTRA_IMAGE_DRAWABLE_ID, defaultErrorActivityDrawableId);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    if (iSave != null) {
                                        iSave.writeCrash(thread, throwable, TAG, getAllErrorDetailsFromIntent(application, intent));
                                    }
                                    application.startActivity(intent);
                                }
                            }
                            final Activity lastActivity = lastActivityCreated.get();
                            if (lastActivity != null) {
                                lastActivity.finish();
                                lastActivityCreated.clear();
                            }
                            killCurrentProcess();
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                            int currentlyStartedActivities = 0;

                            @Override
                            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                                if (activity.getClass() != errorActivityClass) {
                                    lastActivityCreated = new WeakReference<>(activity);
                                }
                            }

                            @Override
                            public void onActivityStarted(Activity activity) {
                                currentlyStartedActivities++;
                                isInBackground = (currentlyStartedActivities == 0);
                                //do nothing
                            }

                            @Override
                            public void onActivityResumed(Activity activity) {
                                //do nothing
                            }

                            @Override
                            public void onActivityPaused(Activity activity) {
                                //do nothing
                            }

                            @Override
                            public void onActivityStopped(Activity activity) {
                                currentlyStartedActivities--;
                                isInBackground = (currentlyStartedActivities == 0);
                            }

                            @Override
                            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                                //do nothing
                            }

                            @Override
                            public void onActivityDestroyed(Activity activity) {
                                //do nothing
                            }
                        });
                    }

                    Log.i(TAG, "CrashManager 已经加载");
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "未知异常", t);
        }
    }

    public static boolean isShowErrorDetailsFromIntent(Intent intent) {
        return intent.getBooleanExtra(CrashManager.EXTRA_SHOW_ERROR_DETAILS, true);
    }

    public static int getDefaultErrorActivityDrawableIdFromIntent(Intent intent) {
        return intent.getIntExtra(CrashManager.EXTRA_IMAGE_DRAWABLE_ID, R.drawable.error_image);
    }

    public static String getStackTraceFromIntent(Intent intent) {
        return intent.getStringExtra(CrashManager.EXTRA_STACK_TRACE);
    }

    public static String getAllErrorDetailsFromIntent(Context context, Intent intent) {
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String versionName = getVersionName(context);
        String errorDetails = "";
        errorDetails += "版本号: " + versionName + NEW_LINE;
        //        errorDetails += "Build date: " + buildDateAsString + " \n";
        errorDetails += "错误时间: " + dateFormat.format(currentDate) + NEW_LINE;
        errorDetails += "设备名称: " + getDeviceModelName() + NEW_LINE;
        errorDetails += "错误原因: " + NEW_LINE;
        errorDetails += getStackTraceFromIntent(intent);
        return errorDetails;
    }

    public static CrashInfo getCrashInfo(Context context, Intent intent) {
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String versionName = getVersionName(context);
        CrashInfo crashInfo = new CrashInfo();
        crashInfo.setDeviceName(getDeviceModelName());
        crashInfo.setVersionName(versionName);
        crashInfo.setCrashDate(dateFormat.format(currentDate));
        crashInfo.setCrashMsg(getStackTraceFromIntent(intent));
        return crashInfo;
    }


    @SuppressWarnings("unchecked")
    public static Class<? extends Activity> getRestartActivityClassFromIntent(Intent intent) {
        Serializable serializedClass = intent.getSerializableExtra(CrashManager.EXTRA_RESTART_ACTIVITY_CLASS);
        if (serializedClass != null && serializedClass instanceof Class) {
            return (Class<? extends Activity>) serializedClass;
        } else {
            return null;
        }
    }

    public static void restartApplicationWithIntent(Activity activity, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.finish();
        activity.startActivity(intent);
        killCurrentProcess();
    }

    public static void closeApplication(Activity activity) {
        activity.finish();
        killCurrentProcess();
    }


    public static boolean isLaunchErrorActivityWhenInBackground() {
        return launchErrorActivityWhenInBackground;
    }

    public static void setLaunchErrorActivityWhenInBackground(boolean launchErrorActivityWhenInBackground) {
        CrashManager.launchErrorActivityWhenInBackground = launchErrorActivityWhenInBackground;
    }

    public static boolean isShowErrorDetails() {
        return showErrorDetails;
    }

    public static void setShowErrorDetails(boolean showErrorDetails) {
        CrashManager.showErrorDetails = showErrorDetails;
    }

    public static int getDefaultErrorActivityDrawable() {
        return defaultErrorActivityDrawableId;
    }

    public static void setDefaultErrorActivityDrawable(int defaultErrorActivityDrawableId) {
        CrashManager.defaultErrorActivityDrawableId = defaultErrorActivityDrawableId;
    }

    public static boolean isEnableAppRestart() {
        return enableAppRestart;
    }

    public static void setEnableAppRestart(boolean enableAppRestart) {
        CrashManager.enableAppRestart = enableAppRestart;
    }

    public static Class<? extends Activity> getErrorActivityClass() {
        return errorActivityClass;
    }

    public static void setErrorActivityClass(Class<? extends Activity> errorActivityClass) {
        CrashManager.errorActivityClass = errorActivityClass;
    }

    public static Class<? extends Activity> getRestartActivityClass() {
        return restartActivityClass;
    }

    public static void setRestartActivityClass(Class<? extends Activity> restartActivityClass) {
        CrashManager.restartActivityClass = restartActivityClass;
    }


    private static boolean isStackTraceLikelyConflictive(Throwable throwable, Class<? extends Activity> activityClass) {
        do {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (("android.app.ActivityThread".equals(element.getClassName()) && "handleBindApplication".equals(element.getMethodName())) || element.getClassName().equals(activityClass.getName())) {
                    return true;
                }
            }
        } while ((throwable = throwable.getCause()) != null);
        return false;
    }

    private static String getBuildDateAsString(Context context, DateFormat dateFormat) {
        String buildDate;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            buildDate = dateFormat.format(new Date(time));
            zf.close();
        } catch (Exception e) {
            buildDate = "Unknown";
        }
        return buildDate;
    }

    private static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private static Class<? extends Activity> guessRestartActivityClass(Context context) {
        Class<? extends Activity> resolvedActivityClass;

        //If action is defined, use that
        resolvedActivityClass = CrashManager.getRestartActivityClassWithIntentFilter(context);

        //Else, get the default launcher activity
        if (resolvedActivityClass == null) {
            resolvedActivityClass = getLauncherActivity(context);
        }

        return resolvedActivityClass;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getRestartActivityClassWithIntentFilter(Context context) {
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                new Intent().setAction(INTENT_ACTION_RESTART_ACTIVITY),
                PackageManager.GET_RESOLVED_FILTER);

        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, e.toString());
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getLauncherActivity(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            try {
                return (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {
                Log.e(TAG, e.toString());
            }
        }

        return null;
    }

    private static Class<? extends Activity> guessErrorActivityClass(Context context) {
        Class<? extends Activity> resolvedActivityClass;
        resolvedActivityClass = CrashManager.getErrorActivityClassWithIntentFilter(context);

        if (resolvedActivityClass == null) {
            resolvedActivityClass = CrashErrorActivity.class;
        }

        return resolvedActivityClass;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getErrorActivityClassWithIntentFilter(Context context) {
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(
                new Intent().setAction(INTENT_ACTION_ERROR_ACTIVITY),
                PackageManager.GET_RESOLVED_FILTER);

        if (resolveInfoList != null && resolveInfoList.size() > 0) {
            ResolveInfo resolveInfo = resolveInfoList.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, e.toString());
            }
        }

        return null;
    }


    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }


}
