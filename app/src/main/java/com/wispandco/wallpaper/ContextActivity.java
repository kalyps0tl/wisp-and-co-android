package com.wispandco.wallpaper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

/** An explainable and permission-light context dashboard. */
public final class ContextActivity extends Activity {
    private static final int ACTIVITY_REQUEST=41;
    private LinearLayout body;
    private int dp(int a){return (int)(a*getResources().getDisplayMetrics().density+.5f);}
    private void text(String msg,int size) {
        TextView label=new TextView(this);
        label.setText(msg);label.setTextColor(0xff554960);label.setTextSize(size);
        label.setPadding(0,dp(7),0,dp(10));body.addView(label);
    }
    private void button(String label,Runnable callback){
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(0xff4e425a);
        GradientDrawable shape=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{0xffe9dcec,0xffd9e6de});
        shape.setCornerRadius(dp(16));b.setBackground(shape);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(55));lp.bottomMargin=dp(10);
        body.addView(b,lp);b.setOnClickListener(v->callback.run());
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);render();}
    private void render(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xfffcf7fb);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(20),dp(32),dp(20),dp(36));scroll.addView(body);
        setContentView(scroll);
        text("✦ How Wisp senses the world",25);
        text(ContextEngine.dashboard(this),16);
        text("Everything here is optional. Context gently influences Wisp; it never judges how much you move.",14);
        button("Where am I?  "+ContextEngine.place(this),()->new AlertDialog.Builder(this)
            .setTitle("Home or away (manual)")
            .setItems(new String[]{"At home","Out for a while","Not specified"},(d,i)->{
                ContextEngine.setPlace(this,new String[]{"home","away","unspecified"}[i]);
                WispState.reactToContext(this);render();
            }).show());
        button("Movement mode: "+ContextEngine.manualMotion(this),()->new AlertDialog.Builder(this)
            .setTitle("What can Wisp assume about movement?")
            .setItems(new String[]{"Automatic when step sensor is enabled","I'm on a walk","I'm taking it easy"},(d,i)->{
                ContextEngine.setMotion(this,new String[]{"automatic","walking","resting"}[i]);
                WispState.reactToContext(this);render();
            }).show());
        button("Step sensor: "+(ContextEngine.enabled(this)?"ON":"OFF"),()->{
            if(ContextEngine.enabled(this)) {
                ContextEngine.setEnabled(this,false);render();return;
            }
            if(!ContextEngine.hasStepSensor(this)){
                new AlertDialog.Builder(this).setTitle("No step counter available")
                    .setMessage("This phone doesn't report a step-counter sensor. You can still tell Wisp when you're walking using the manual movement option.")
                    .setPositiveButton("Okay",null).show();return;
            }
            if(!ContextEngine.hasPermission(this)) {
                new AlertDialog.Builder(this).setTitle("Optional physical activity permission")
                    .setMessage("Wisp will use step events only while its app or live wallpaper is visible. No GPS routes, exercise logs, step totals or uploaded activity data. Denying permission doesn't restrict gameplay.")
                    .setPositiveButton("Allow and continue",(d,i)->
                        requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},ACTIVITY_REQUEST))
                    .setNegativeButton("Not now",null).show();
            } else {
                ContextEngine.setEnabled(this,true);render();
            }
        });
        button("Gentle animation / battery saver: "+(ContextEngine.reducedMotion(this)?"ON":"OFF"),()->{
            boolean newValue=!WispState.prefs(this).getBoolean("reduce_motion",false);
            ContextEngine.setReducedMotion(this,newValue);render();
        });
        button("Refresh context summary",this::render);
        text("Automatic movement requires the phone's step-counter hardware, activity permission and Wisp being visible. It will show 'unknown' rather than assume you're resting when it has no recent evidence.",13);
        text("Home versus away is intentionally manual in this version. Wisp does not collect your address, follow your route or use background GPS.",13);
        text("Battery saver always reduces animation automatically, even if gentle animation is switched off.",13);
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==ACTIVITY_REQUEST){
            ContextEngine.setEnabled(this,results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED);
            render();
        }
    }
    @Override protected void onResume(){super.onResume();ContextEngine.startVisible(this);render();}
    @Override protected void onPause(){ContextEngine.stopVisible(this);super.onPause();}
}
