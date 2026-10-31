package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User-owned, on-device import and export. Uses Android's document picker,
 * which requires no storage permission and does not upload any data.
 */
public final class WispBackup {
    private WispBackup(){}
    public static final String FORMAT="wisp-and-co-save";
    public static final int VERSION=1;
    private static final int MAX_BYTES=262144;
    private static final Set<String> KEYS=new HashSet<>(Arrays.asList(
        "name","spirit","profile","matched_profile","matching_complete","matching_dismissed",
        "zone","activity","weather","liveWeather","note",
        "slot","projects","projectProgress","affection","journal","discoveries",
        "evolution_stage","evolution_trait","evolution_hold","evolution_history",
        "event_pumpkins","event_christmas","event_hanukkah",
        "pumpkin_progress","tree_progress","hanukkah_progress","seasonal_idle_ticks",
        "sensor_enabled","place_mode","motion_override","reduce_motion",
        "quiet_evenings",
        "moment_pending","moment_last_offered","moment_last_answer",
        "moment_invites","moment_snoozed",
        "affinity_curiosity","affinity_making","affinity_kindness",
        "neighbor_0_bond","neighbor_1_bond","neighbor_2_bond",
        "neighbor_3_bond","neighbor_0_seen","neighbor_1_seen","neighbor_2_seen",
        "neighbor_3_seen","neighbor_0_last","neighbor_1_last","neighbor_2_last",
        "neighbor_3_last","neighbor_0_gift","neighbor_1_gift","neighbor_2_gift",
        "neighbor_3_gift"
    ));
    private static boolean allowed(String key) {
        return KEYS.contains(key) ||
            (key.startsWith("hotspot_") && key.endsWith("_last")
                && key.length()<=64 && key.matches("[a-z0-9_]+"));
    }
    private static JSONObject build(Context c) throws Exception {
        SharedPreferences p=WispState.prefs(c);
        JSONObject values=new JSONObject();
        for(Map.Entry<String,?> entry:p.getAll().entrySet()){
            if(!allowed(entry.getKey())||entry.getValue()==null)continue;
            Object value=entry.getValue();
            JSONObject typed=new JSONObject();
            if(value instanceof String) {
                if(((String)value).length()>12000)continue;
                typed.put("type","string");
            }else if(value instanceof Integer) typed.put("type","int");
            else if(value instanceof Long)typed.put("type","long");
            else if(value instanceof Boolean)typed.put("type","boolean");
            else continue;
            typed.put("value",value);values.put(entry.getKey(),typed);
        }
        JSONObject root=new JSONObject();
        root.put("format",FORMAT);root.put("schema",VERSION);
        root.put("contents",values);
        root.put("description","A local Wisp & Co save file. Store it somewhere private.");
        return root;
    }
    public static int exportTo(Context c,Uri destination) throws Exception {
        JSONObject root=build(c);
        byte[] bytes=root.toString(2).getBytes(StandardCharsets.UTF_8);
        if(bytes.length>MAX_BYTES)throw new IllegalArgumentException("Save is unexpectedly large.");
        try(OutputStream out=c.getContentResolver().openOutputStream(destination,"wt")){
            if(out==null)throw new IllegalArgumentException("Can't open the selected destination.");
            out.write(bytes);out.flush();
        }
        return root.getJSONObject("contents").length();
    }
    public static JSONObject readValidated(Context c,Uri source) throws Exception {
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        try(InputStream in=c.getContentResolver().openInputStream(source)){
            if(in==null)throw new IllegalArgumentException("Can't open selected file.");
            byte[] chunk=new byte[4096];int read,total=0;
            while((read=in.read(chunk))!=-1){
                total+=read;
                if(total>MAX_BYTES)throw new IllegalArgumentException("That file is too large to be a Wisp save.");
                buffer.write(chunk,0,read);
            }
        }
        JSONObject root=new JSONObject(new String(buffer.toByteArray(),StandardCharsets.UTF_8));
        if(!FORMAT.equals(root.optString("format"))||root.optInt("schema")!=VERSION)
            throw new IllegalArgumentException("This is not a compatible Wisp & Co save.");
        JSONObject values=root.getJSONObject("contents");
        if(values.length()>KEYS.size()+40)throw new IllegalArgumentException("Save contains too many entries.");
        // Validate every known entry *before* changing any existing state.
        for(java.util.Iterator<String> keys=values.keys();keys.hasNext();){
            String key=keys.next();
            if(!allowed(key))continue;
            JSONObject item=values.getJSONObject(key);
            String type=item.getString("type");
            Object value=item.get("value");
            if(("string".equals(type) && value instanceof String && ((String)value).length()<=12000)
                ||("int".equals(type)&&value instanceof Integer)
                ||("long".equals(type)&&value instanceof Number)
                ||("boolean".equals(type)&&value instanceof Boolean)) continue;
            throw new IllegalArgumentException("Invalid value in save: "+key);
        }
        return values;
    }
    public static int importFrom(Context c,JSONObject values) throws Exception {
        SharedPreferences.Editor editor=WispState.prefs(c).edit();
        int restored=0;
        for(java.util.Iterator<String> it=values.keys();it.hasNext();){
            String key=it.next();if(!allowed(key))continue;
            JSONObject entry=values.getJSONObject(key);
            String type=entry.getString("type");
            if("string".equals(type))editor.putString(key,entry.getString("value"));
            else if("int".equals(type))editor.putInt(key,entry.getInt("value"));
            else if("long".equals(type))editor.putLong(key,entry.getLong("value"));
            else if("boolean".equals(type))editor.putBoolean(key,entry.getBoolean("value"));
            else continue;
            restored++;
        }
        // Never import transient walking evidence. It may be stale from another device.
        editor.remove("last_walk").remove("weatherUpdated").remove("tempC");
        // Importing a save never grants sensor permission or enables motion
        // sensing on the destination phone. Opt-in again in Context & senses.
        editor.putBoolean("sensor_enabled",false);
        if(!editor.commit())throw new IllegalStateException("Could not save the imported data.");
        ContextEngine.refresh(c);
        return restored;
    }
    public static void erase(Context c) {
        WispState.prefs(c).edit().clear().commit();
        ContextEngine.refresh(c);
    }
}
