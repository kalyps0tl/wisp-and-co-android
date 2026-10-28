package com.wispandco.wallpaper;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Open the five locations and interact with the current companion. */
public final class ExplorerActivity extends Activity {
    private final String[] zones={"dock","garden","pond","forest","cottage"};
    private final String[] names={"Dock","Garden","Pond","Forest","Inside cottage"};
    private SceneView scene;
    private TextView caption;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        FrameLayout root=new FrameLayout(this);
        scene=new SceneView();root.addView(scene,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(18),dp(20),dp(18),dp(14));top.setBackgroundColor(0x889988a2);
        TextView header=new TextView(this);header.setText("✦ Wisp's Living World");header.setTextColor(0xffffffff);header.setTextSize(22);
        top.addView(header);
        caption=new TextView(this);caption.setTextColor(0xfff8f2f5);caption.setTextSize(14);
        top.addView(caption);root.addView(top,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
        HorizontalScrollView strip=new HorizontalScrollView(this);
        strip.setHorizontalScrollBarEnabled(false);strip.setBackgroundColor(0xcca398a7);
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(6),dp(8),dp(6),dp(8));
        for(int i=0;i<zones.length;i++){
            final String selected=zones[i];
            Button button=new Button(this);button.setAllCaps(false);button.setText(names[i]);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(54));lp.setMargins(dp(3),0,dp(3),0);
            row.addView(button,lp);
            button.setOnClickListener(v->{WispState.set(this,"zone",selected);update();});
        }
        strip.addView(row);
        FrameLayout.LayoutParams bottom=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);
        root.addView(strip,bottom);setContentView(root);update();
    }
    private int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+.5f);}
    private void update(){
        WispState.advance(this);
        if(caption!=null)caption.setText(WispState.name(this)+" is "+WispState.activity(this)+". Tap to pet.  "+WispState.weather(this));
        if(scene!=null)scene.invalidate();
    }
    private final class SceneView extends View {
        private final WorldRenderer renderer=new WorldRenderer();
        SceneView(){super(ExplorerActivity.this);}
        @Override protected void onDraw(Canvas canvas){
            renderer.draw(canvas,getWidth(),getHeight(),WispState.zone(ExplorerActivity.this),
                WispState.weather(ExplorerActivity.this),WispState.spirit(ExplorerActivity.this),System.currentTimeMillis());
            postInvalidateDelayed(85);
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            if(event.getAction()==MotionEvent.ACTION_UP){WispState.pet(ExplorerActivity.this);update();return true;}
            return true;
        }
    }
    @Override protected void onResume(){super.onResume();WeatherClient.refresh(this);update();}
}
