package com.wispandco.wallpaper;

import android.service.wallpaper.WallpaperService;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/** Native Android live wallpaper. Character and wildlife are animated independently of scenery. */
public final class WispWallpaper extends WallpaperService {
    @Override public Engine onCreateEngine() { return new WispEngine(); }
    private final class WispEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final WorldRenderer renderer=new WorldRenderer();
        private final Paint labels=new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean visible=false;
        private boolean contextSubscribed=false;
        private final Runnable tick=this::render;
        private long lastRefresh=0;
        WispEngine(){
            setTouchEventsEnabled(true);
            labels.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        }
        private void manageContextSubscription(boolean desired) {
            if(desired && !contextSubscribed) {
                ContextEngine.startVisible(WispWallpaper.this);
                contextSubscribed=true;
            } else if(!desired && contextSubscribed) {
                ContextEngine.stopVisible(WispWallpaper.this);
                contextSubscribed=false;
            }
        }
        @Override public void onVisibilityChanged(boolean isVisible) {
            visible=isVisible;
            manageContextSubscription(isVisible);
            handler.removeCallbacks(tick);
            if(visible)render();
        }
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height) {
            super.onSurfaceChanged(holder,format,width,height);if(visible)render();
        }
        @Override public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);visible=false;manageContextSubscription(false);handler.removeCallbacks(tick);
        }
        @Override public void onDestroy() {
            visible=false;manageContextSubscription(false);handler.removeCallbacks(tick);renderer.release();super.onDestroy();
        }
        @Override public void onTouchEvent(MotionEvent e) {
            if(e.getAction()==MotionEvent.ACTION_UP) {WispState.pet(WispWallpaper.this);render();}
            super.onTouchEvent(e);
        }
        private void render(){
            final long frameStart=SystemClock.uptimeMillis();
            handler.removeCallbacks(tick);
            if(!visible)return;
            long now=System.currentTimeMillis();
            if(now-lastRefresh>30000){WispState.advance(WispWallpaper.this);WeatherClient.refresh(WispWallpaper.this);lastRefresh=now;}
            SurfaceHolder surface=getSurfaceHolder();
            if(surface==null)return;
            Canvas c=null;
            try {
                c=surface.lockCanvas();
                if(c==null)return;
                int w=c.getWidth(),h=c.getHeight();
                renderer.draw(c,w,h,WispState.zone(WispWallpaper.this),
                    WispState.weather(WispWallpaper.this),WispState.spirit(WispWallpaper.this),now,
                    WispEvolution.stage(WispWallpaper.this),WispEvolution.trait(WispWallpaper.this),
                    WorldEvents.selected(WispWallpaper.this,0)?WorldEvents.progress(WispWallpaper.this,0):0,
                    WorldEvents.selected(WispWallpaper.this,1)?WorldEvents.progress(WispWallpaper.this,1):0,
                    WorldEvents.selected(WispWallpaper.this,2)?WorldEvents.progress(WispWallpaper.this,2):0);
                // Weather on the wallpaper, not only inside settings.
                SharedPreferences p=WispState.prefs(WispWallpaper.this);
                String wx=WispState.weather(WispWallpaper.this);
                String label=wx.toUpperCase(java.util.Locale.US);
                if(p.contains("tempC")) label+=" · "+p.getInt("tempC",0)+"°C";
                labels.setColor(0xff5f5269);labels.setTextSize(Math.max(22,w*.037f));
                c.drawText(label,w*.066f,h*.105f,labels);
                if(p.getLong("weatherUpdated",0)>0 && now-p.getLong("weatherUpdated",0)>90*60*1000L){
                    labels.setTextSize(Math.max(14,w*.019f));c.drawText("Last known weather",w*.066f,h*.126f,labels);
                }
            }catch(Exception ignored){ /* Surface can disappear during home-screen switches. */ }
            finally {if(c!=null)try{surface.unlockCanvasAndPost(c);}catch(Exception ignored){}}
            // Aim for 30 FPS while visible; never busy-loop if a frame runs long.
            int frameTarget=ContextEngine.reducedMotion(WispWallpaper.this)?100:33;
            if(visible)handler.postDelayed(tick,Math.max(1,frameTarget-(SystemClock.uptimeMillis()-frameStart)));
        }
    }
}
