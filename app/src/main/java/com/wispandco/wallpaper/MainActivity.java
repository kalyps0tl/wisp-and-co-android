package com.wispandco.wallpaper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.graphics.drawable.GradientDrawable;

public final class MainActivity extends Activity {
    private LinearLayout body;
    private final int INK=0xff51445e;
    private final String[] spirits={"Woodland ears","Little antlers","Moth wings","Leaf sprout"};
    private final String[] temperaments={"Wayfinder — curious","Dreamweaver — creative","Hearthling — gentle"};
    @Override public void onCreate(Bundle b){super.onCreate(b);render();}
    private int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+0.5f);}
    private TextView text(String value,int size){
        TextView t=new TextView(this);t.setText(value);t.setTextColor(INK);t.setTextSize(size);
        t.setPadding(0,dp(5),0,dp(8));body.addView(t);return t;
    }
    private void button(String label,Runnable action){
        Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextColor(INK);
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xfff5dbe8,0xffdcd6f6});
        d.setCornerRadius(dp(18));b.setBackground(d);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(55));lp.bottomMargin=dp(10);
        body.addView(b,lp);b.setOnClickListener(v->action.run());
    }
    private void render(){
        WispState.advance(this);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xfffbf5fa);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(24),dp(24),dp(24),dp(40));
        scroll.addView(body);setContentView(scroll);
        text("✦ Wisp & Co.",29);
        text("A little world that keeps living when you are away.",15);
        text("Your companion: "+WispState.name(this)+"  ·  "+WispState.profile(this)+"  ·  "+WispState.spirit(this),16);
        SharedPreferences p=WispState.prefs(this);
        String degree=p.contains("tempC")?("  ·  "+p.getInt("tempC",0)+"°C"):"";
        text("Weather: "+WispState.weather(this)+degree+"  ·  "+(p.getBoolean("liveWeather",false)?"live (opt-in)":"manual preview"),15);
        text("Currently "+WispState.activity(this)+" at the "+WispState.zone(this)+".",15);
        text("Making: "+WispState.currentProject(this)+" ("+WispState.projectProgress(this)+"/4)  ·  Creations: "+WispState.projects(this),14);
        text("✉ "+WispState.note(this),15);
        button("Explore cottage, pond, garden and forest",()->startActivity(new Intent(this,ExplorerActivity.class)));
        button("Set the living wallpaper",()->{
            Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,WispWallpaper.class));
            startActivity(i);
        });
        button("Pet Wisp",()->{WispState.pet(this);render();});
        button("Meet your Wisp · matching quiz",this::quiz);
        button("Customize spirit form",()->new AlertDialog.Builder(this).setTitle("Which little spirit?")
            .setItems(spirits,(d,i)->{WispState.set(this,"spirit",WispState.SPIRITS[i]);render();}).show());
        button("Choose Wisp's name",()->{
            EditText edit=new EditText(this);edit.setSingleLine(true);edit.setText(WispState.name(this));
            new AlertDialog.Builder(this).setTitle("Your companion's name").setView(edit)
              .setPositiveButton("Save",(d,i)->{String n=edit.getText().toString().trim();if(!n.isEmpty())WispState.set(this,"name",n);render();})
              .setNegativeButton("Cancel",null).show();
        });
        button("Preview different weather",()->new AlertDialog.Builder(this).setTitle("Preview weather")
            .setItems(new String[]{"Clear","Cloudy","Rain","Snow","Fog","Storm"},(d,i)->{
                String[] options={"clear","cloud","rain","snow","fog","storm"};
                p.edit().putBoolean("liveWeather",false).putString("weather",options[i]).remove("tempC").remove("weatherUpdated").apply();render();
            }).show());
        button("Use real local weather (optional)",()->{
            p.edit().putBoolean("liveWeather",true).putLong("weatherUpdated",0).apply();
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},7);
            else WeatherClient.refresh(this);
            render();
        });
        button("Simulate Wisp's next activities",()->{
            // A preview shortcut, not a timer or paid currency.
            p.edit().putLong("slot",System.currentTimeMillis()/WispState.SLOT-4).apply();
            WispState.advance(this);render();
        });
        button("Choose seasonal celebrations",()->new AlertDialog.Builder(this).setTitle("Events you want in Wisp's world")
            .setMultiChoiceItems(new String[]{"Autumn pumpkin carving","Christmas tree decorating","Hanukkah traditions"},
                new boolean[]{p.getBoolean("event_pumpkins",false),p.getBoolean("event_christmas",false),p.getBoolean("event_hanukkah",false)},
                (d,which,checked)->{
                    String[] keys={"event_pumpkins","event_christmas","event_hanukkah"};
                    p.edit().putBoolean(keys[which],checked).apply();
                }).setPositiveButton("Done",null).show());
        text("Foundation build: native scenes and movement. The seasonal choices are saved; full event scenes and detailed evolution are still being developed.",12);
    }
    private void quiz(){
        final int[] score={0,0,0};
        String[] prompts={"Something unusual appears by the pond. You'd rather...",
            "Which quiet afternoon sounds most magical?","A friend's creation goes wrong. You'd rather..."};
        String[][] answers={{"Follow the mystery","Try building with it","Sit and observe"},
            {"Explore a new path","Make something cozy","Share tea by the fire"},
            {"Encourage another attempt","Invent a solution","Offer comfort"}};
        quizStep(0,score,prompts,answers);
    }
    private void quizStep(int step,int[] score,String[] prompts,String[][] answers){
        if(step==3){
            int best=0;for(int i=1;i<3;i++)if(score[i]>score[best])best=i;
            final int winner=best;
            new AlertDialog.Builder(this).setTitle("A Wisp approaches...")
                .setMessage("You seem well matched with a "+temperaments[winner]+". This is only a suggestion; you may choose any personality.")
                .setPositiveButton("Meet this Wisp",(d,w)->{String[] ids={"wonder","maker","hearth"};WispState.set(this,"profile",ids[winner]);render();})
                .setNeutralButton("Choose another",(d,w)->new AlertDialog.Builder(this).setTitle("Choose a temperament")
                    .setItems(temperaments,(dialog,choice)->{String[] ids={"wonder","maker","hearth"};WispState.set(this,"profile",ids[choice]);render();}).show())
                .show();return;
        }
        new AlertDialog.Builder(this).setTitle("Meeting Wisp · "+(step+1)+"/3")
            .setMessage(prompts[step])
            .setItems(answers[step],(d,i)->{score[i]++;quizStep(step+1,score,prompts,answers);})
            .setNegativeButton("Maybe later",null).show();
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(request,permissions,grants);
        if(request==7 && grants.length>0 && grants[0]==PackageManager.PERMISSION_GRANTED)WeatherClient.refresh(this);
    }
    @Override protected void onResume(){super.onResume();WeatherClient.refresh(this);}
}
