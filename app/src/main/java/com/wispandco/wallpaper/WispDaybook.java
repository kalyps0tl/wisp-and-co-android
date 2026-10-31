package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.Random;

/** One offline storybook postcard per local calendar day, with no nudges or alerts. */
public final class WispDaybook {
    private WispDaybook(){}
    private static final String[][] GREETINGS={
        {"Dear friend,","A little note from our cottage,","Hello, companion of little adventures,"},
        {"The woods were full of quiet possibilities today.","I've been listening to the world outside.","Something in the little world made me smile today."},
        {"Some days a small discovery is enough.","I wonder which path will surprise us next.","There is no hurry to see everything."}
    };
    public static boolean available(Context c){
        return LocalDate.now().toEpochDay()==WispState.prefs(c).getLong("daybook_date",Long.MIN_VALUE);
    }
    public static String peek(Context c){
        return available(c)?WispState.prefs(c).getString("daybook_letter",""):"";
    }
    public static synchronized String read(Context c){
        SharedPreferences p=WispState.prefs(c);
        long day=LocalDate.now().toEpochDay();
        if(p.getLong("daybook_date",Long.MIN_VALUE)==day)return p.getString("daybook_letter","");
        Random r=new Random(day*31883L+WispState.profile(c).hashCode()*37L+
            WispState.name(c).hashCode());
        String intro=GREETINGS[0][r.nextInt(GREETINGS[0].length)];
        String scene=GREETINGS[1][r.nextInt(GREETINGS[1].length)];
        String finishing=GREETINGS[2][r.nextInt(GREETINGS[2].length)];
        String weather=WispState.weather(c);
        String sky="rain".equals(weather)?"Raindrops have been tapping on the leaves.":
            "snow".equals(weather)?"The air feels hushed under the snow.":
            "storm".equals(weather)?"The thunder made me grateful for the warm hearth.":
            "fog".equals(weather)?"Mist has been curling around our paths.":
            "cloud".equals(weather)?"The clouds are soft and slow.":
            "The light has been playing across the pond.";
        String focus;
        String favored=WispMoments.favored(c);
        if("making".equals(favored))focus="I have been thinking about my next little handmade invention.";
        else if("curiosity".equals(favored))focus="I keep wondering what waits beyond the familiar trail.";
        else if("kindness".equals(favored))focus="I want to make something thoughtful for our woodland neighbors.";
        else if("dreamer".equals(WispState.profile(c)))focus="I think the stars have a few stories left to tell.";
        else if("maker".equals(WispState.profile(c)))focus="My workbench is covered in promising bits and pieces.";
        else if("wonder".equals(WispState.profile(c)))focus="Somewhere, a tiny secret path is waiting to be noticed.";
        else focus="I like making our little home feel welcoming.";
        String neighbor=WorldNeighbors.friendship(c,r.nextInt(WorldNeighbors.NAMES.length));
        int blooms=GardenPatch.bloomingCount(c);
        String garden=blooms>0?
            "Our garden has "+blooms+" blooming bed"+(blooms==1?"":"s")+". I think the bees will be pleased.":
            GardenPatch.plantedCount(c)>0?
            "The little garden is growing in its own time. We don't need to rush it.":
            "The garden beds are waiting for seeds. Maybe someday we will choose a favorite.";
        String ending="I am still learning who I want to become. Thank you for making room for that.\n\n"+
            "With little wishes,\n"+WispState.name(c)+" ✦";
        String letter=intro+"\n\n"+scene+" "+sky+"\n\n"+focus+" "+
            garden+"\n\n"+finishing+"\n\n"+ending;
        p.edit().putLong("daybook_date",day).putString("daybook_letter",letter).apply();
        WorldJournal.add(c,"A new little letter was written by "+WispState.name(c)+".");
        return letter;
    }
}
