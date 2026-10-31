package com.wispandco.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;

/** Visible, explorable objects in each location; no separate server needed. */
public final class WorldHotspots {
    private WorldHotspots(){}
    public static final class Point {
        public final String id,label,description,kind;
        public final float x,y,radius;
        public final int neighbor;
        Point(String id,String label,String description,float x,float y,float radius,String kind,int neighbor){
            this.id=id;this.label=label;this.description=description;this.x=x;this.y=y;
            this.radius=radius;this.kind=kind;this.neighbor=neighbor;
        }
    }
    private static Point p(String id,String label,String description,float x,float y,String kind,int neighbor){
        return new Point(id,label,description,x,y,35,kind,neighbor);
    }
    public static Point[] forZone(String zone){
        if("dock".equals(zone))return new Point[]{
            p("dock_cottage","Cottage door","A lantern glows behind the little cottage window.",112,531,"cottage",-1),
            p("dock_water","Shimmering river","Sunlight and clouds are reflected in the slow river.",320,777,"discover",-1)
        };
        if("pond".equals(zone))return new Point[]{
            p("pond_frog","Pip the frog","A little green frog is waiting by the lilies.",320,732,"friend",0),
            p("pond_dragonfly","Dragonflies","Silvery wings move above the water.",168,517,"discover",-1),
            p("pond_reeds","Whispering reeds","A soft hush comes from the reeds.",65,658,"discover",-1)
        };
        if("garden".equals(zone))return new Point[]{
            p("garden_bee","Bramble the bee","Someone is visiting the sweet wildflowers.",145,593,"friend",1),
            p("garden_flowers","Wildflower patch","A little patch of daisies and lavender.",87,715,"discover",-1),
            p("garden_door","Cottage door","The door is open to Wisp's warm home.",303,535,"cottage",-1)
        };
        if("forest".equals(zone))return new Point[]{
            p("forest_owl","Alder the owl","A sleepy owl is perched among the branches.",310,461,"friend",2),
            p("forest_mushroom","Mushroom circle","Tiny mushrooms crowd together in the moss.",110,699,"discover",-1),
            p("forest_path","Secret path","This path bends between the old trunks.",200,624,"discover",-1)
        };
        return new Point[]{
            p("cottage_books","Bookshelf","Books full of little memories and half-written stories.",100,360,"journal",-1),
            p("cottage_moth","Fenn the moth","A gentle moth has made the bookshelf its home.",169,373,"friend",3),
            p("cottage_fire","Hearth","The cottage fireplace is warm and welcoming.",309,473,"rest",-1),
            p("cottage_bench","Workbench","An unfinished little project rests on the table.",154,663,"project",-1),
            p("cottage_window","Window","The weather outside paints the windows different colors.",246,267,"window",-1)
        };
    }
    public static Point find(String zone,float x,float y){
        Point nearest=null;float best=Float.MAX_VALUE;
        for(Point point:forZone(zone)){
            float dx=x-point.x,dy=y-point.y,d=dx*dx+dy*dy;
            if(d<point.radius*point.radius && d<best){nearest=point;best=d;}
        }
        return nearest;
    }
    public static String interact(Context c,Point point){
        if("friend".equals(point.kind))return WorldNeighbors.visit(c,point.neighbor);
        if("journal".equals(point.kind))return WorldJournal.recent(c);
        if("cottage".equals(point.kind))return "The little cottage is just beyond the path. Tap the path sign to enter.";
        SharedPreferences prefs=WispState.prefs(c);
        if("project".equals(point.kind)){
            int next=WispState.projectProgress(c)+1,done=WispState.projects(c);
            String name=WispState.currentProject(c);
            if(next>=4){
                done++;next=0;
                prefs.edit().putInt("projects",done).putInt("projectProgress",next).apply();
                WorldJournal.add(c,"Finished "+name+" together at the workbench.");
                WispEvolution.refresh(c);
                return "Wisp finishes "+name+"! A tiny handmade treasure now belongs in the cottage.";
            }
            prefs.edit().putInt("projectProgress",next).apply();
            return "Wisp works on "+name+". Little by little, it is taking shape ("+next+"/4).";
        }
        if("rest".equals(point.kind)){
            String note="Wisp settles beside the crackling fire, listening to its own quiet thoughts.";
            prefs.edit().putString("activity","resting by the hearth").apply();
            return note;
        }
        if("window".equals(point.kind)){
            return "Outside, it is "+WispState.weather(c)+". Wisp watches the sky and wonders what tomorrow might bring.";
        }
        String note="Wisp and you explore "+point.description.toLowerCase();
        String key="hotspot_"+point.id+"_last";
        long now=System.currentTimeMillis(),previous=prefs.getLong(key,0);
        if(previous==0||now-previous>12L*60L*60L*1000L){
            prefs.edit().putLong(key,now).apply();
            WispEvolution.discover(c,point.label);
            WorldJournal.add(c,"Found "+point.label+" in the "+WispState.zone(c)+".");
            return note+" Something about it feels new.";
        }
        return note+" There's always something comforting about familiar places.";
    }
}
