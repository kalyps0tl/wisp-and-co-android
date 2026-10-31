package com.wispandco.wallpaper;

/** Explicit connected-path graph for navigation between the five local scenes. */
public final class SceneRoutes {
    private SceneRoutes() {}
    public static final String[] ZONES={"dock","pond","forest","garden","cottage"};
    public static final String[] LABELS={"Dock","Pond","Forest","Garden","Cottage"};
    public static final String[][] EDGES={
        {"pond","garden","cottage"},
        {"dock","forest"},
        {"pond","garden"},
        {"forest","dock","cottage"},
        {"garden","dock"}
    };
    public static int index(String z){
        for(int i=0;i<ZONES.length;i++)if(ZONES[i].equals(z))return i;
        return 0;
    }
    public static String title(String z){return LABELS[index(z)];}
    public static String[] paths(String z){return EDGES[index(z)].clone();}
    public static String left(String z){
        String[] edges=paths(z);return edges[0];
    }
    public static String right(String z){
        String[] edges=paths(z);return edges[edges.length-1];
    }
    public static boolean connects(String a,String b){
        for(String e:paths(a))if(e.equals(b))return true;
        return false;
    }
}
