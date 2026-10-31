package com.wispandco.wallpaper;

import android.content.Context;
import java.util.Calendar;

/** Safe optional visual preferences. No location, device model or new permissions. */
public final class WorldVisuals {
    private WorldVisuals(){}
    public static final String[] LIGHT_MODES={"auto","day","night"};
    public static final String[] PALETTES={"original","lavender","moss","golden"};
    public static final String[] LIGHT_LABELS={"Follow local clock","Always daytime","Always moonlit"};
    public static final String[] PALETTE_LABELS={"Original storybook","Lavender dreams","Woodland moss","Golden hour"};
    public static String lighting(Context c){
        return WispState.prefs(c).getString("world_light","auto");
    }
    public static String palette(Context c){
        return WispState.prefs(c).getString("world_palette","original");
    }
    public static void setLighting(Context c,int choice){
        if(choice>=0&&choice<LIGHT_MODES.length){
            WispState.prefs(c).edit().putString("world_light",LIGHT_MODES[choice]).apply();
        }
    }
    public static void setPalette(Context c,int choice){
        if(choice>=0&&choice<PALETTES.length){
            WispState.prefs(c).edit().putString("world_palette",PALETTES[choice]).apply();
        }
    }
    public static boolean isNight(String mode){
        if("day".equals(mode))return false;
        if("night".equals(mode))return true;
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour>=19||hour<6;
    }
    public static int overlay(String palette){
        if("lavender".equals(palette))return 0x28a58dc5;
        if("moss".equals(palette))return 0x2391b795;
        if("golden".equals(palette))return 0x2effd4a0;
        return 0x00000000;
    }
}
