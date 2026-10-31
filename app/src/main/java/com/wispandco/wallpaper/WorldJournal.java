package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.DateFormat;
import java.util.Date;

/** A compact, private and persistent record of Wisp's discoveries. */
public final class WorldJournal {
    private WorldJournal() {}
    public static synchronized void add(Context c,String event){
        SharedPreferences p=WispState.prefs(c);
        String previous=p.getString("journal","");
        String entry=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT)
            .format(new Date())+"  ·  "+event.replace("\n"," ").replace("|"," ");
        String combined=entry+"\n"+previous;
        if(combined.length()>9000)combined=combined.substring(0,9000);
        p.edit().putString("journal",combined).apply();
    }
    public static String recent(Context c){
        String j=WispState.prefs(c).getString("journal","");
        return j.isEmpty()?"A blank book, full of possible stories.":j;
    }
}
