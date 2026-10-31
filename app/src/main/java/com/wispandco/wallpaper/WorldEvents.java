package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/** Opt-in, persistent celebrations with tangible visible progress. */
public final class WorldEvents {
    private WorldEvents(){}
    public static final String[] KEYS={"event_pumpkins","event_christmas","event_hanukkah"};
    public static final String[] LABELS={"Carve a pumpkin","Decorate the winter tree","Add a Hanukkah candle"};
    private static final String[] STEPS={"pumpkin_progress","tree_progress","hanukkah_progress"};
    public static int progress(Context c,int which){return WispState.prefs(c).getInt(STEPS[which],0);}
    public static boolean selected(Context c,int which){return WispState.prefs(c).getBoolean(KEYS[which],false);}
    public static int maximum(int which){return which==0?4:which==1?7:8;}
    public static String status(Context c,int which){
        return LABELS[which]+" ("+progress(c,which)+"/"+maximum(which)+")";
    }
    public static synchronized void takePart(Context c,int which){
        if(which<0||which>2)return;
        SharedPreferences p=WispState.prefs(c);
        if(!selected(c,which)){
            p.edit().putString("note","We can celebrate that, if you'd like to switch it on in settings.").apply();
            return;
        }
        int next=Math.min(maximum(which),progress(c,which)+1);
        String note;
        if(which==0)note=next==4?"Our pumpkin has a proper little face now!":"I carved another careful piece of our pumpkin.";
        else if(which==1)note=next==7?"The tree sparkles with all our handmade ornaments!":"I hung a decoration on our winter tree.";
        else note="We've prepared the candle display for night "+next+". We can learn about Hanukkah together.";
        // Lighting is an opt-in visual activity, not a substitute for religious observance.
        p.edit().putInt(STEPS[which],next).putString("note",note).apply();
        WispEvolution.discover(c, "cottage");
    }
    public static void seasonalAutonomy(Context c){
        SharedPreferences p=WispState.prefs(c);
        int month=Calendar.getInstance().get(Calendar.MONTH);
        // Wisp can independently prepare decorations in October and December.
        // Hanukkah dates change by year: never infer them from Gregorian month.
        int which=month==Calendar.OCTOBER?0:month==Calendar.DECEMBER?1:-1;
        if(which<0 || !selected(c,which) || progress(c,which)>=maximum(which))return;
        int count=p.getInt("seasonal_idle_ticks",0)+1;
        p.edit().putInt("seasonal_idle_ticks",count).apply();
        if(count%5==0)takePart(c,which);
    }
}
