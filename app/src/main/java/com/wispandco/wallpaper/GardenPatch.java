package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;

/** Three permanent seed beds, real calendar-day growth, no withering or streaks. */
public final class GardenPatch {
    private GardenPatch(){}
    public static final String[] NAMES={"Moonbells","Copperleaf","Starglow","Lavender","Firefly fern"};
    public static final String[] DESCRIPTIONS={
        "Little pearl-colored flowers that open toward the moon.",
        "Amber leaves that curl like tiny autumn ribbons.",
        "Star-shaped violet blossoms with a gentle shimmer.",
        "Soft purple flowers beloved by Bramble the bee.",
        "A lush fern with pale dots that resemble fireflies."
    };
    public static final int[] BLOOM_DAYS={4,3,5,3,4};
    public static long today(){return LocalDate.now().toEpochDay();}
    public static int kind(Context c,int plot){
        if(plot<0||plot>2)return -1;
        int index=WispState.prefs(c).getInt("garden_"+plot+"_kind",-1);
        return index>=0&&index<NAMES.length?index:-1;
    }
    public static int age(Context c,int plot){
        if(kind(c,plot)<0)return 0;
        long planted=WispState.prefs(c).getLong("garden_"+plot+"_planted",today());
        return (int)Math.min(365,Math.max(0,today()-planted));
    }
    public static int phase(Context c,int plot){
        int k=kind(c,plot);if(k<0)return -1;
        int days=age(c,plot);
        if(days>=BLOOM_DAYS[k])return 3;
        if(days>=2)return 2;
        if(days>=1)return 1;
        return 0;
    }
    public static String phaseName(Context c,int plot){
        int stage=phase(c,plot);
        return stage<0?"Empty garden bed":stage==0?"Fresh seed":
            stage==1?"First sprout":stage==2?"Growing leaves":"In bloom";
    }
    public static String description(Context c,int plot){
        int k=kind(c,plot);
        return k<0?"This patch is ready for a new little seed.":
            NAMES[k]+" · "+phaseName(c,plot)+" · "+
            DESCRIPTIONS[k]+" A flower grows in its own time, even while you're away.";
    }
    public static int plantedCount(Context c){
        int planted=0;for(int i=0;i<3;i++)if(kind(c,i)>=0)planted++;
        return planted;
    }
    public static int bloomingCount(Context c){
        int blooming=0;for(int i=0;i<3;i++)if(phase(c,i)>=3)blooming++;
        return blooming;
    }
    public static String overview(Context c){
        int count=plantedCount(c);
        return count==0?"Three empty garden beds, full of possibility.":
            bloomingCount(c)+" of "+count+" planted beds blooming.";
    }
    public static void plant(Context c,int plot,int kind){
        if(plot<0||plot>2||kind<0||kind>=NAMES.length)return;
        SharedPreferences p=WispState.prefs(c);
        p.edit().putInt("garden_"+plot+"_kind",kind)
            .putLong("garden_"+plot+"_planted",today())
            .putString("note","I planted "+NAMES[kind]+" with you! We can check its leaves another day.")
            .apply();
        WorldJournal.add(c,"Planted "+NAMES[kind]+" in garden bed "+(plot+1)+".");
        WispWidget.refresh(c);
    }
    public static void admire(Context c,int plot){
        int kind=kind(c,plot);
        if(kind<0)return;
        String note=phase(c,plot)>=3?
            "Look! Our "+NAMES[kind]+" is in bloom. I think Bramble will love it.":
            "I checked our "+NAMES[kind]+". Slow growing things deserve patience.";
        WispState.prefs(c).edit().putString("note",note).apply();
    }
}
