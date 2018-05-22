package co.digitalwing.autostartpermissions;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.widget.AppCompatCheckBox;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import static co.digitalwing.autostartpermissions.Defs.APP_NAME;

public class AutoStartPermissions extends ReactContextBaseJavaModule {
    public AutoStartPermissions(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "AutoStartPermissions";
    }

    private static List<Intent> POWERMANAGER_INTENTS = new ArrayList<>(Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(Uri.parse("mobilemanager://function/entry/AutoStart"))
    ));

    private static void StartPowerSaverIntent(Context context, Callback callback)
    {
        SharedPreferences settings = context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE);
        boolean skipMessage = settings.getBoolean("skipAppListMessage", false);
        if (!skipMessage)
        {
            final SharedPreferences.Editor editor = settings.edit();
            for (Intent intent : POWERMANAGER_INTENTS) {
                if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null)
                {
                    AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(context);
                    dontShowAgain.setText("Do not show again");
                    dontShowAgain.setOnCheckedChangeListener((compoundButton, b) -> {
                        editor.putBoolean("skipAppListMessage", compoundButton.isChecked());
                        editor.apply();
                    });

                    new AlertDialog.Builder(context)
                            .setIcon(context.getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                            .setTitle(String.format("Add %s to list", getAppNameFromManifest(context)))
                            .setMessage(String.format("%s requires to be enabled/added in the list to function properly.\n", getAppNameFromManifest(context)))
                            .setView(dontShowAgain)
                            .setPositiveButton("Go to settings", (o, d) -> {
                                context.startActivity(intent);
                                callback.invoke(true);
                            })
                            .setNegativeButton(android.R.string.cancel, (o, d) -> {
                                callback.invoke(false);
                            })
                            .show();

                    break;
                }
            }
        }
    }

    protected String getAppNameFromManifest(context) {
        final ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(APP_NAME);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOGTAG, "Failed to resolve app name from manifest", e);
            return null;
        }
    }

    @ReactMethod
    public void check(
            Callback completionCallback) {

        ReactApplicationContext context = getReactApplicationContext();
        StartPowerSaverIntent(context, completionCallback);
    }
}
