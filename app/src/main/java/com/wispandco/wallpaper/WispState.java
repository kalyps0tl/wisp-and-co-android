package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Random;

/** Private on-device personality, activity, weather, and project state. */
public final class WispState {
    private WispState() {}
    public static final String[] ZONES = {"dock","garden","pond","forest","cottage"};
    public static final String[] SPIRITS = {"woodland","antlered","moth","sprout"};
    public static final String[] PROJECTS = {"a tiny bridge","a leaf journal","a mushroom planter","a pond lantern","a cozy scarf","a frog-sized boat","a patchwork blanket","a flower press"};
    static final String PREFS = "wisp_world";
    static final long SLOT = 20 * 60 * 1000L;

    public static SharedPreferences prefs(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
    public static String weather(Context c) { return prefs(c).getString("weather", "clear"); }
    public static String spirit(Context c) { return prefs(c).getString("spirit", "woodland"); }
    public static String profile(Context c) { return prefs(c).getString("profile", "hearth"); }
    public static String name(Context c) { return prefs(c).getString("name", "Wisp"); }
    public static String zone(Context c) { return prefs(c).getString("zone", "dock"); }
    public static String activity(Context c) { return prefs(c).getString("activity", "watching the pond"); }
    public static int projects(Context c) { return prefs(c).getInt("projects", 0); }
    public static int projectProgress(Context c) { return prefs(c).getInt("projectProgress", 0); }
    public static int affection(Context c) { return prefs(c).getInt("affection", 0); }
    public static String note(Context c) { return prefs(c).getString("note", "I wonder where the dragonflies sleep?"); }
    public static void set(Context c, String key, String value) { prefs(c).edit().putString(key,value).apply(); }
    public static void pet(Context c) {
        SharedPreferences p=prefs(c);
        p.edit().putInt("affection", Math.min(999,p.getInt("affection",0)+1))
            .putString("note", "I was just about to tell you what I found by the pond!").apply();
    }
    public static synchronized void advance(Context c) {
        SharedPreferences p=prefs(c);
        long now=System.currentTimeMillis(),last=p.getLong("slot",0);
        if (last==0) { p.edit().putLong("slot",now/SLOT).apply(); return; }
        long slot=now/SLOT, delta=Math.min(48,Math.max(0,slot-last));
        if (delta==0) return;
        int complete=p.getInt("projects",0),progress=p.getInt("projectProgress",0);
        String wx=weather(c),type=profile(c),zone=p.getString("zone","dock"),activity=p.getString("activity","reading");
        String note=p.getString("note","");
        for (int i=0;i<delta;i++) {
            Random r=new Random((last+i+1)*104729L + type.hashCode() * 31L);
            int n=r.nextInt(100);
            if ("storm".equals(wx)) zone = r.nextBoolean()?"cottage":"dock";
            else if ("snow".equals(wx)) zone = r.nextInt(3)==0?"garden":"cottage";
            else if ("rain".equals(wx)) zone = r.nextInt(3)==0?"pond":(r.nextBoolean()?"cottage":"dock");
            else if ("wonder".equals(type)) zone=ZONES[r.nextInt(5)];
            else if ("maker".equals(type)) zone=r.nextInt(3)==0?"garden":"cottage";
            else zone=ZONES[r.nextInt(5)];
            if ("storm".equals(wx)) activity="building a cozy little nest";
            else if ("snow".equals(wx)) activity="studying snowflakes";
            else if ("rain".equals(wx)) activity="watching rain make ripples";
            else if ("pond".equals(zone)) activity=n<60?"visiting the frog":"counting dragonflies";
            else if ("garden".equals(zone)) activity=n<60?"tending wildflowers":"following a bee";
            else if ("forest".equals(zone)) activity=n<60?"finding mushroom paths":"gathering leaves";
            else if ("cottage".equals(zone)) activity=n<60?"making something by the hearth":"reading";
            else activity=n<60?"watching the pond":"looking for shiny stones";
            if (r.nextInt(4)==0) {
                progress++;
                if (progress>=4) {
                    progress=0; complete++;
                    note="I finished "+PROJECTS[(complete-1)%PROJECTS.length]+"! Want to come see?";
                }
            }
            if (r.nextInt(18)==0) note="I found something new today. Can I show you?";
            else if (r.nextInt(22)==0) note="My idea did not work. Could I have some encouragement?";
            else if (r.nextInt(30)==0) note="I misplaced a keepsake. Would you sit with me for a moment?";
        }
        p.edit().putLong("slot",slot).putString("zone",zone).putString("activity",activity)
            .putInt("projectProgress",progress).putInt("projects",complete).putString("note",note).apply();
    }
    public static String currentProject(Context c) { return PROJECTS[projects(c)%PROJECTS.length]; }
}
