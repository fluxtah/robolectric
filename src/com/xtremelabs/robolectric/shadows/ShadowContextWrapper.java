package com.xtremelabs.robolectric.shadows;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.test.mock.MockPackageManager;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class ShadowContextWrapper extends ShadowContext {
    @RealObject private ContextWrapper realContextWrapper;
    private Context baseContext;

    private MockPackageManager packageManager;

    private String packageName;

    public void __constructor__(Context baseContext) {
        this.baseContext = baseContext;
    }

    @Implementation
    public Context getApplicationContext() {
        return baseContext.getApplicationContext();
    }

    @Implementation
    public Resources getResources() {
        return getApplicationContext().getResources();
    }

    @Implementation
    public ContentResolver getContentResolver() {
        return getApplicationContext().getContentResolver();
    }

    @Implementation
    public Object getSystemService(String name) {
        return getApplicationContext().getSystemService(name);
    }

    @Implementation
    public void sendBroadcast(Intent intent) {
        getApplicationContext().sendBroadcast(intent);
    }

    @Implementation
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return ((ShadowApplication) shadowOf(getApplicationContext())).registerReceiverWithContext(receiver, filter, realContextWrapper);
    }

    @Implementation
    public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        getApplicationContext().unregisterReceiver(broadcastReceiver);
    }

    @Implementation
    public String getPackageName() {
        return realContextWrapper == getApplicationContext() ? packageName : getApplicationContext().getPackageName();
    }

    /**
     * Implements Android's {@code MockPackageManager} with an anonymous inner class.
     * @return a {@code MockPackageManager}
     */
    @Implementation
    public PackageManager getPackageManager() {
        if (packageManager == null) {
            packageManager = new MockPackageManager() {
                public PackageInfo packageInfo;
                public ArrayList<PackageInfo> packageList;

                @Override
                public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
                    ensurePackageInfo();
                    return packageInfo;
                }

                @Override
                public List<PackageInfo> getInstalledPackages(int flags) {
                    ensurePackageInfo();
                    if (packageList == null) {
                        packageList = new ArrayList<PackageInfo>();
                        packageList.add(packageInfo);
                    }
                    return packageList;
                }

                private void ensurePackageInfo() {
                    if (packageInfo == null) {
                        packageInfo = new PackageInfo();
                        packageInfo.packageName = packageName;
                        packageInfo.versionName = "1.0";
                    }
                }

            };
        }
        return packageManager;
    }

    @Implementation
    public ComponentName startService(Intent service) {
        return getApplicationContext().startService(service);
    }

    @Implementation
    public void startActivity(Intent intent) {
        getApplicationContext().startActivity(intent);
    }

    @Implementation
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new TestSharedPreferences(name, mode);
    }

    /**
     * Non-Android accessor that delegates to the application to consume and return the next {@code Intent} on the
     * started activities stack.
     *
     * @return the next started {@code Intent} for an activity
     */
    public Intent getNextStartedActivity() {
        return getShadowApplication().getNextStartedActivity();
    }

    /**
     * Non-Androic accessor that delegates to the application to return (without consuming) the next {@code Intent} on
     * the started activities stack.
     *
     * @return the next started {@code Intent} for an activity
     */
    public Intent peekNextStartedActivity() {
        return getShadowApplication().peekNextStartedActivity();
    }

    /**
     * Non-Android accessor that delegates to the application to consume and return the next {@code Intent} on the
     * started services stack.
     *
     * @return the next started {@code Intent} for a service
     */
    public Intent getNextStartedService() {
        return getShadowApplication().getNextStartedService();
    }

    /**
     * Non-Android accessor that delegates to the application to return (without consuming) the next {@code Intent} on
     * the started services stack.
     *
     * @return the next started {@code Intent} for a service
     */
    public Intent peekNextStartedService() {
        return getShadowApplication().peekNextStartedService();
    }
    /**
     * Non-Android accessor that is used at start-up to set the package name
     *
     * @return the next started {@code Intent} for a service
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    private ShadowApplication getShadowApplication() {
        return ((ShadowApplication) shadowOf(getApplicationContext()));
    }
}
