package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/**
 * Four recurring neighbors with local memories, gentle social progression and
 * believable routines. Nothing depends on streaks or daily attendance.
 */
public final class WorldNeighbors {
    private WorldNeighbors() {}
    public static final String[] NAMES={"Pip the pond frog","Bramble the honeybee","Alder the forest owl","Fenn the cottage moth"};
    public static final String[] PLACES={"pond","garden","forest","cottage"};
    public static final String[] FAVORITES={
        "smooth river stones and quiet reed music",
        "flowers, sunny mornings and little garden projects",
        "old stories, mushrooms and the rustle of midnight trees",
        "soft lanternlight, woven things and the warm cottage"
    };
    public static int index(String zone) {
        for(int i=0;i<PLACES.length;i++)if(PLACES[i].equals(zone))return i;
        return -1;
    }
    public static String name(int n){return NAMES[n];}
    public static int bond(Context c,int n){
        return WispState.prefs(c).getInt("neighbor_"+n+"_bond",0);
    }
    public static int meetings(Context c,int n){
        return WispState.prefs(c).getInt("neighbor_"+n+"_seen",0);
    }
    public static String friendship(Context c,int n){
        int b=bond(c,n);
        return b>=7?"Old friends":b>=4?"Good friends":b>=2?"Growing friendship":"New acquaintance";
    }
    public static String schedule(Context c,int n){
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean rainy="rain".equals(WispState.weather(c));
        boolean storm="storm".equals(WispState.weather(c));
        if(n==0)return storm?"Pip is sheltering under the bank":rainy?"Pip enjoys the puddles":
            hour>=6&&hour<20?"Pip rests near the lily pads":"Pip watches the moonlit water";
        if(n==1)return (hour>=7&&hour<18&&!rainy&&!storm)?
            "Bramble is visiting flowers":"Bramble is sleeping in a sheltered blossom";
        if(n==2)return hour>=19||hour<6?
            "Alder is awake among the branches":"Alder is dozing peacefully";
        return hour>=17||hour<7?
            "Fenn is fluttering near the lamplight":"Fenn is resting among folded blankets";
    }
    public static String greeting(Context c,int n){
        int b=bond(c,n);
        if(b>=7)return new String[]{
            "Pip has saved a smooth pebble for Wisp. They watch the pond together.",
            "Bramble leads Wisp to a secret wildflower patch.",
            "Alder shares an old tale about a forgotten woodland trail.",
            "Fenn has woven a tiny ribbon for Wisp's favourite book."}[n];
        if(b>=3)return new String[]{
            "Pip hops over to say hello and shows Wisp a new ripple pattern.",
            "Bramble pauses to tell Wisp which flowers are sweetest.",
            "Alder recognizes Wisp's footsteps and gives a sleepy hoot.",
            "Fenn flutters near Wisp and settles beside the fireplace."}[n];
        return new String[]{
            "Pip peeks from behind a lily pad, curious but cautious.",
            "Bramble makes a careful circle around a lavender flower.",
            "Alder regards Wisp from a branch, one eye barely open.",
            "Fenn hides behind a book, then shyly waves a wing."}[n];
    }
    public static String visit(Context c,int n){
        if(n<0||n>=NAMES.length)return "";
        SharedPreferences p=WispState.prefs(c);
        long now=System.currentTimeMillis();
        long previous=p.getLong("neighbor_"+n+"_last",0);
        int seen=meetings(c,n)+1;
        int b=bond(c,n);
        // A friendship can deepen no more than once every ~4 hours. The player
        // can still visit as often as they please, with no penalties or streaks.
        if(previous==0||now-previous>=4L*60L*60L*1000L)b=Math.min(12,b+1);
        String description=greeting(c,n);
        p.edit().putInt("neighbor_"+n+"_bond",b)
            .putInt("neighbor_"+n+"_seen",seen)
            .putLong("neighbor_"+n+"_last",now)
            .putString("note",description).apply();
        WorldJournal.add(c,"Visited "+NAMES[n]+". "+description);
        WispEvolution.discover(c,PLACES[n]);
        // Evolution may announce itself; restore this shared encounter as the
        // latest immediate conversation.
        p.edit().putString("note",description).apply();
        return description;
    }
    public static String summary(Context c){
        StringBuilder out=new StringBuilder();
        for(int i=0;i<NAMES.length;i++){
            if(i>0)out.append("\n\n");
            out.append(NAMES[i]).append(" — ").append(friendship(c,i));
            out.append("\n").append(schedule(c,i));
            out.append("\nVisits: ").append(meetings(c,i));
        }
        return out.toString();
    }
}
