package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Random;

/**
 * Wisp's voluntary little life questions. Each question appears at most once
 * until acknowledged, never requires a response and never punishes absence.
 * Answers add character preferences and journal entries; they are not metrics
 * of the human's health, mood, or fitness.
 */
public final class WispMoments {
    private WispMoments() {}
    public static final String[][] PROMPTS={
        {"I found a very tiny door under the old willow. Should I...",
            "Knock and see who's home","Build a little welcome sign","Leave a flower outside"},
        {"Pip seems lonely today. I could...",
            "Take Pip on a pond adventure","Make a tiny lily-pad boat","Sit quietly beside Pip"},
        {"The night is full of fireflies. I wonder if...",
            "They know any hidden paths","I could draw their patterns","They might like a warm lantern"},
        {"The cottage shelf is overflowing with discoveries!",
            "Sort them into a map","Make a display case","Share them with my friends"},
        {"I'm not sure my new creation turned out how I imagined.",
            "Try something wild and different","See what the pieces can become","Rest and return to it later"},
        {"The rain turned the forest silver. Should I...",
            "Look for a sheltered trail","Collect rainwater for flowers","Watch it from the cottage"},
        {"Alder mentioned a story nobody remembers.",
            "Explore the place it happened","Write it into our little book","Ask Alder to tell us slowly"},
        {"I have a sprout that won't quite grow. Maybe...",
            "Find it a sunnier spot","Make a new planter","Give it gentle care and patience"},
        {"Someone left a sweet note by the pond.",
            "Follow the tiny footprints","Write a note in return","Leave a little keepsake"},
        {"What should I do with a bright, unusual stone?",
            "Follow the river for more","Make something with it","Keep it by the hearth"},
        {"I think I'd like to learn something new.",
            "Understand the forest paths","Try making something by hand","Get to know another creature"},
        {"Tonight feels like the start of a story.",
            "A journey over the hills","A handmade little surprise","A gathering around the fire"}
    };
    public static final String[] KINDS={"curiosity","making","kindness"};
    public static boolean pending(Context c) {
        return WispState.prefs(c).getInt("moment_pending",-1)>=0;
    }
    public static int pendingIndex(Context c) {
        int index=WispState.prefs(c).getInt("moment_pending",-1);
        return index>=0&&index<PROMPTS.length?index:-1;
    }
    public static String question(Context c) {
        int i=pendingIndex(c);
        return i<0?"Wisp is quietly following its own ideas.":PROMPTS[i][0];
    }
    public static String[] answers(Context c) {
        int i=pendingIndex(c);
        if(i<0)return new String[0];
        return new String[]{PROMPTS[i][1],PROMPTS[i][2],PROMPTS[i][3]};
    }
    public static int affinity(Context c,int which){
        return WispState.prefs(c).getInt("affinity_"+KINDS[which],0);
    }
    public static String favored(Context c){
        int best=0;
        for(int i=1;i<3;i++)if(affinity(c,i)>affinity(c,best))best=i;
        int top=affinity(c,best);
        return top==0?"Still discovering its interests":KINDS[best];
    }
    /** User-initiated check-in: no need to wait for an automatic moment. */
    public static synchronized void invite(Context c){
        SharedPreferences p=WispState.prefs(c);
        if(pending(c)){
            p.edit().putBoolean("moment_snoozed",false).apply();
            return;
        }
        long next=p.getLong("moment_invites",0L);
        int index=(int)(next%PROMPTS.length);
        p.edit().putInt("moment_pending",index)
            .putLong("moment_invites",next+1)
            .putLong("moment_last_offered",System.currentTimeMillis()/WispState.SLOT)
            .putBoolean("moment_snoozed",false)
            .putString("note",PROMPTS[index][0]).apply();
    }
    public static synchronized void consider(Context c,long slot,Random random){
        SharedPreferences p=WispState.prefs(c);
        if(pending(c)||p.getBoolean("moment_snoozed",false))return;
        long previous=p.getLong("moment_last_offered",0);
        if(slot-previous<9)return; // At most one new question roughly every three hours.
        if(random.nextInt(8)!=0)return;
        int index=(int)Math.floorMod(slot+WispState.projects(c)+random.nextInt(PROMPTS.length),PROMPTS.length);
        p.edit().putInt("moment_pending",index).putLong("moment_last_offered",slot)
            .putString("note",PROMPTS[index][0]).apply();
    }
    public static synchronized String reply(Context c,int choice){
        int i=pendingIndex(c);
        if(i<0||choice<0||choice>=3)return "";
        SharedPreferences p=WispState.prefs(c);
        String selected=PROMPTS[i][choice+1];
        int old=affinity(c,choice);
        String response=choice==0?
            "That sounds wonderfully adventurous! I'll keep an eye out for hidden trails.":
            choice==1?
            "You've given me an idea. I might try making something entirely new!":
            "I like the way you make space for gentle things. I'll remember that.";
        p.edit().putInt("moment_pending",-1).putBoolean("moment_snoozed",false)
            .putInt("affinity_"+KINDS[choice],Math.min(100,old+1))
            .putString("moment_last_answer",selected)
            .putString("note",response).apply();
        WorldJournal.add(c,"You suggested: "+selected+". Wisp replied: "+response);
        return response;
    }
    public static synchronized void later(Context c){
        // The prompt remains available whenever the user feels like talking.
        WispState.prefs(c).edit().putBoolean("moment_snoozed",true).apply();
    }
    public static synchronized void resume(Context c) {
        WispState.prefs(c).edit().putBoolean("moment_snoozed",false).apply();
    }
    public static synchronized void dismiss(Context c){
        WispState.prefs(c).edit().putInt("moment_pending",-1)
            .putBoolean("moment_snoozed",false).apply();
    }
}
