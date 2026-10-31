package com.wispandco.wallpaper;

import org.junit.Test;
import static org.junit.Assert.*;

/** Ensures every path has a reciprocal exit and all scenes are connected. */
public final class SceneRoutesTest {
    @Test public void everyRouteIsReciprocal(){
        for(String from:SceneRoutes.ZONES)for(String to:SceneRoutes.paths(from)){
            assertTrue(from+" -> "+to+" has no return path",SceneRoutes.connects(to,from));
        }
    }
    @Test public void eachPlaceCanBeReachedFromDock(){
        for(String destination:SceneRoutes.ZONES) {
            java.util.ArrayDeque<String> pending=new java.util.ArrayDeque<>();
            java.util.HashSet<String> visited=new java.util.HashSet<>();
            pending.add("dock");
            while(!pending.isEmpty()){
                String at=pending.removeFirst();
                if(!visited.add(at))continue;
                for(String next:SceneRoutes.paths(at))pending.addLast(next);
            }
            assertTrue("Unreachable scene: "+destination,visited.contains(destination));
        }
    }
    @Test public void noPlaceLinksToItself(){
        for(String zone:SceneRoutes.ZONES)assertFalse(SceneRoutes.connects(zone,zone));
    }
}
