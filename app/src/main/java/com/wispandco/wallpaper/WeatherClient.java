package com.wispandco.wallpaper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.SharedPreferences;
import android.os.Build;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/** Weather is optional; approximate location only; fetched on demand and cached locally. */
public final class WeatherClient {
    private WeatherClient() {}
    private static final java.util.concurrent.ExecutorService EXEC=Executors.newSingleThreadExecutor();
    private static volatile boolean fetching=false;
    public static void refresh(Context c) {
        Context app=c.getApplicationContext();
        if (fetching) return;
        SharedPreferences p=WispState.prefs(app);
        if (!p.getBoolean("liveWeather",false)) return;
        if (System.currentTimeMillis()-p.getLong("weatherUpdated",0)<30*60*1000L) return;
        if (app.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) return;
        LocationManager lm=(LocationManager)app.getSystemService(Context.LOCATION_SERVICE);
        if (lm==null) return;
        Location loc=null;
        try {
            for(String provider: lm.getProviders(true)) {
                Location item=lm.getLastKnownLocation(provider);
                if(item!=null && (loc==null || item.getTime()>loc.getTime())) loc=item;
            }
        }catch(SecurityException ignored){return;}
        if(loc==null) return;
        double lat=loc.getLatitude(),lon=loc.getLongitude();
        fetching=true;
        EXEC.execute(()-> {
            HttpURLConnection conn=null;
            try {
                String u=String.format(Locale.US,"https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f&current=temperature_2m,weather_code&timezone=auto",lat,lon);
                conn=(HttpURLConnection)new URL(u).openConnection();
                conn.setConnectTimeout(8000);conn.setReadTimeout(8000);
                try(InputStream in=conn.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                    byte[] b=new byte[4096];int n;
                    while((n=in.read(b))>0)out.write(b,0,n);
                    JSONObject wx=new JSONObject(out.toString("UTF-8")).getJSONObject("current");
                    int code=wx.getInt("weather_code");
                    String state=classify(code);
                    WispState.prefs(app).edit().putString("weather",state)
                        .putInt("tempC",(int)Math.round(wx.getDouble("temperature_2m")))
                        .putLong("weatherUpdated",System.currentTimeMillis()).apply();
                }
            }catch(Exception ignored){ /* Preserve most recent cached weather if offline. */ }
            finally {if(conn!=null)conn.disconnect();fetching=false;}
        });
    }
    private static String classify(int code) {
        if(code==0||code==1)return "clear";
        if(code==2||code==3)return "cloud";
        if(code==45||code==48)return "fog";
        if(code>=95)return "storm";
        if(code>=71&&code<=86)return "snow";
        if(code>=51&&code<=67||code>=80&&code<=82)return "rain";
        return "cloud";
    }
}
