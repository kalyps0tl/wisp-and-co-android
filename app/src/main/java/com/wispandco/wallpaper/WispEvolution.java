package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Physical changes are earned by experiences, never by logging in on a timer.
 * The stage can be reverted and held. No state is lost when the user opts out.
 */
public final class WispEvolution {
    private WispEvolution() {}
    public static final String[] TRAITS={"starlight","moss","petals","frost","embers"};
    public static final String[] STAGES={"Little wanderer","Bright-eyed explorer","Blooming spirit","Keeper of wonders","Ancient little soul"};
    public static int stage(Context c){return WispState.prefs(c).getInt("evolution_stage",0);}
    public static String trait(Context c){return WispState.prefs(c).getString("evolution_trait","starlight");}
    public static boolean held(Context c){return WispState.prefs(c).getBoolean("evolution_hold",false);}
    public static int discoveries(Context c){return WispState.prefs(c).getInt("discoveries",0);}
    public static String description(Context c){
        return STAGES[Math.min(4,stage(c))]+" · "+trait(c)+" markings"+(held(c)?" (evolution paused)":"");
    }
    public static synchronized boolean refresh(Context c){
        SharedPreferences p=WispState.prefs(c);
        if(p.getBoolean("evolution_hold",false))return false;
        int score=WispState.projects(c)*2+WispState.affection(c)/5+discoveries(c)*3;
        int[] thresholds={4,10,20,36};
        int current=stage(c),next=current;
        while(next<4 && score>=thresholds[next])next++;
        if(next==current)return false;
        String previous=p.getString("evolution_history","");
        String entry=current+"~"+trait(c);
        String history=previous.isEmpty()?entry:previous+"|"+entry;
        String wx=WispState.weather(c),zone=WispState.zone(c);
        String newTrait="snow".equals(wx)?"frost":
            "forest".equals(zone)?"moss":"garden".equals(zone)?"petals":
            "cottage".equals(zone)?"embers":"starlight";
        p.edit().putString("evolution_history",history).putInt("evolution_stage",next)
            .putString("evolution_trait",newTrait)
            .putString("note","Something new is growing on me: "+newTrait+"! You can change me back any time.").apply();
        return true;
    }
    public static synchronized boolean undo(Context c){
        SharedPreferences p=WispState.prefs(c);
        String history=p.getString("evolution_history","");
        if(history.isEmpty())return false;
        int last=history.lastIndexOf('|');
        String entry=history.substring(last+1);
        String[] pair=entry.split("~",2);
        int previous;
        try{previous=Integer.parseInt(pair[0]);}catch(Exception e){return false;}
        p.edit().putInt("evolution_stage",Math.max(0,Math.min(4,previous)))
            .putString("evolution_trait",pair.length>1?pair[1]:"starlight")
            .putString("evolution_history",last<0?"":history.substring(0,last))
            .putBoolean("evolution_hold",true)
            .putString("note","I can grow in my own time. This shape feels nice too!").apply();
        return true;
    }
    public static void resume(Context c){
        WispState.prefs(c).edit().putBoolean("evolution_hold",false).apply();
        refresh(c);
    }
    public static void discover(Context c,String where){
        SharedPreferences p=WispState.prefs(c);
        int count=p.getInt("discoveries",0)+1;
        p.edit().putInt("discoveries",count)
            .putString("note","I learned something new at the "+where+". I'll remember our visit!").apply();
        refresh(c);
    }
}
