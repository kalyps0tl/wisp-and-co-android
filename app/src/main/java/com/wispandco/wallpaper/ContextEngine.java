package com.wispandco.wallpaper;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import java.util.Calendar;

/**
 * Opt-in, phone-only context. No service, hidden location history, routes,
 * uploads or background tracking. The step sensor is subscribed to only
 * while an app screen or the live wallpaper is visible.
 */
public final class ContextEngine implements SensorEventListener {
    private static final long WALK_FRESH_MS = 7L * 60L * 1000L;
    private static ContextEngine instance;
    private final Context app;
    private final SensorManager sensors;
    private final Sensor stepSensor;
    private int visibleOwners = 0;
    private boolean listening = false;
    private float lastRawSteps = -1f;
    private long previousSampleAt = 0L;

    private ContextEngine(Context c) {
        app=c.getApplicationContext();
        sensors=(SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        stepSensor=sensors==null?null:sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }
    private static synchronized ContextEngine engine(Context c) {
        if(instance==null)instance=new ContextEngine(c);
        return instance;
    }
    public static void startVisible(Context c) {
        ContextEngine e=engine(c);
        synchronized(e) {e.visibleOwners++;e.refreshListener();}
    }
    public static void stopVisible(Context c) {
        ContextEngine e=engine(c);
        synchronized(e) {e.visibleOwners=Math.max(0,e.visibleOwners-1);e.refreshListener();}
    }
    public static void refresh(Context c) {
        ContextEngine e=engine(c);
        synchronized(e) {e.refreshListener();}
    }
    private void refreshListener() {
        boolean permitted=app.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            ==PackageManager.PERMISSION_GRANTED;
        boolean shouldListen=visibleOwners>0 && permitted && stepSensor!=null &&
            WispState.prefs(app).getBoolean("sensor_enabled",false);
        if(shouldListen && !listening) {
            // SENSOR_DELAY_NORMAL lets the platform batch step events as it sees fit.
            listening=sensors.registerListener(this,stepSensor,SensorManager.SENSOR_DELAY_NORMAL);
            lastRawSteps=-1f;previousSampleAt=0;
        } else if (!shouldListen && listening) {
            sensors.unregisterListener(this);
            listening=false;
            lastRawSteps=-1f;
            previousSampleAt=0;
        }
    }
    public static boolean hasStepSensor(Context c) {
        return engine(c).stepSensor!=null;
    }
    public static boolean hasPermission(Context c) {
        return c.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            ==PackageManager.PERMISSION_GRANTED;
    }
    public static boolean enabled(Context c) {
        return WispState.prefs(c).getBoolean("sensor_enabled",false);
    }
    public static void setEnabled(Context c,boolean enabled) {
        WispState.prefs(c).edit().putBoolean("sensor_enabled",enabled).apply();
        if(!enabled)WispState.prefs(c).edit().remove("last_walk").apply();
        refresh(c);
    }
    public static String manualMotion(Context c) {
        return WispState.prefs(c).getString("motion_override","automatic");
    }
    public static void setMotion(Context c,String value) {
        WispState.prefs(c).edit().putString("motion_override",value).apply();
    }
    public static String place(Context c) {
        return WispState.prefs(c).getString("place_mode","unspecified");
    }
    public static void setPlace(Context c,String value) {
        WispState.prefs(c).edit().putString("place_mode",value).apply();
    }
    public static String motion(Context c) {
        String override=manualMotion(c);
        if("walking".equals(override))return "walking";
        if("resting".equals(override))return "resting";
        if(!enabled(c)||!hasPermission(c)||!hasStepSensor(c))return "unknown";
        long last=WispState.prefs(c).getLong("last_walk",0L);
        return last>0 && System.currentTimeMillis()-last<WALK_FRESH_MS?"walking":"unknown";
    }
    public static String dayPart() {
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour>=5 && hour<12)return "morning";
        if(hour>=12 && hour<17)return "afternoon";
        if(hour>=17 && hour<21)return "evening";
        return "night";
    }
    public static boolean batterySaver(Context c) {
        PowerManager power=(PowerManager)c.getSystemService(Context.POWER_SERVICE);
        return power!=null && power.isPowerSaveMode();
    }
    public static boolean reducedMotion(Context c) {
        return WispState.prefs(c).getBoolean("reduce_motion",false) || batterySaver(c);
    }
    public static void setReducedMotion(Context c,boolean yes) {
        WispState.prefs(c).edit().putBoolean("reduce_motion",yes).apply();
    }
    public static String batteryStatus(Context c) {
        Intent status=c.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(status==null)return batterySaver(c)?"Battery saver on":"Battery unavailable";
        int plugged=status.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
        int level=status.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale=status.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        int pct=(level>=0 && scale>0)?(int)(100f*level/scale):-1;
        String state=plugged!=0?"charging":"on battery";
        if(batterySaver(c))state+=", saver on";
        return (pct<0?"":pct+"% · ")+state;
    }
    public static String dashboard(Context c) {
        String source="walking".equals(manualMotion(c)) || "resting".equals(manualMotion(c))
            ?"manual":"automatic";
        String movement=motion(c);
        String sensing;
        if(!enabled(c))sensing="Off";
        else if(!hasStepSensor(c))sensing="No step-counter hardware";
        else if(!hasPermission(c))sensing="Permission needed";
        else sensing="On while Wisp is visible";
        return "Time: "+dayPart()+" (device clock, not actual sunrise)\n"+
            "Where: "+place(c)+" (set manually)\n"+
            "Movement: "+movement+" ("+source+")\n"+
            "Step sensor: "+sensing+"\n"+
            "Battery: "+batteryStatus(c)+"\n"+
            "Gentle animation: "+(reducedMotion(c)?"yes":"no");
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy) {}

    @Override public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()!=Sensor.TYPE_STEP_COUNTER || event.values.length<1)return;
        float raw=event.values[0];
        long now=System.currentTimeMillis();
        if(lastRawSteps>=0 && raw>lastRawSteps && previousSampleAt>0 &&
            now-previousSampleAt<10L*60L*1000L) {
            SharedPreferences p=WispState.prefs(app);
            p.edit().putLong("last_walk",now).apply();
            // Never store a total or expose a step ranking. Only a recent walking hint.
        }
        lastRawSteps=raw;
        previousSampleAt=now;
    }
}
