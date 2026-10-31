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
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        render();
        // The meeting is an invitation, not a permission wall: skipping it
        // leaves all established identity, preferences and progress unchanged.
        if(b==null && !WispState.prefs(this).getBoolean("matching_complete",false)
                   && !WispState.prefs(this).getBoolean("matching_dismissed",false)){
            body.post(()->startActivity(new Intent(this,MatchingActivity.class)));
        }
    }
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
        boolean met=prefsMet();
        text(met?"Your companion: "+WispState.name(this)+" · "+WispState.profile(this)+" · "+WispState.spirit(this):
            "A little spirit is ready to meet you.",16);
        button(met?"Relive the matching ceremony · Meet another Wisp":"MEET YOUR WISP · The matching ceremony ✦",
            ()->startActivity(new Intent(this,MatchingActivity.class)));
        SharedPreferences p=WispState.prefs(this);
        String degree=p.contains("tempC")?("  ·  "+p.getInt("tempC",0)+"°C"):"";
        text("Weather: "+WispState.weather(this)+degree+"  ·  "+(p.getBoolean("liveWeather",false)?"live (opt-in)":"manual preview"),15);
        text("Currently "+WispState.activity(this)+" at the "+WispState.zone(this)+".",15);
        text("Making: "+WispState.currentProject(this)+" ("+WispState.projectProgress(this)+"/4)  ·  Creations: "+WispState.projects(this),14);
        text("✉ "+WispState.note(this),15);
        text("Growing: "+WispEvolution.description(this)+"  ·  Discoveries: "+WispEvolution.discoveries(this),15);
        text("Wisp senses: "+ContextEngine.dayPart()+" · "+
            ContextEngine.place(this)+" · movement "+ContextEngine.motion(this)+
            " · "+(ContextEngine.batterySaver(this)?"battery saver active":"normal battery mode"),13);
        button("Context & senses · your controls",()->
            startActivity(new Intent(this,ContextActivity.class)));
        text("EXPLORE THE LIVING WORLD",18);
        button("Explore Wisp's living world",()->startActivity(new Intent(this,ExplorerActivity.class)));
        button("Visit our woodland neighbors",()->new AlertDialog.Builder(this)
            .setTitle("Friends around the cottage")
            .setMessage(WorldNeighbors.summary(this))
            .setPositiveButton("Go and visit",(d,i)->
                startActivity(new Intent(this,ExplorerActivity.class)))
            .setNegativeButton("Close",null).show());
        button("Read our discovery journal",()->new AlertDialog.Builder(this)
            .setTitle("The little book of days")
            .setMessage(WorldJournal.recent(this))
            .setPositiveButton("Close",null).show());
        button("Set the living wallpaper",()->{
            Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,WispWallpaper.class));
            startActivity(i);
        });
        text("CARE AND CUSTOMIZE",18);
        button("Pet Wisp",()->{WispState.pet(this);render();});
        button("Customize spirit form",()->new AlertDialog.Builder(this).setTitle("Which little spirit?")
            .setItems(spirits,(d,i)->{WispState.set(this,"spirit",WispState.SPIRITS[i]);render();}).show());
        button("Customize Wisp's magical markings",()->new AlertDialog.Builder(this)
            .setTitle("A little bit of magic")
            .setItems(new String[]{"Starlight freckles","Mossy green","Flower petals","Frost shimmer","Warm embers"},
                (dialog,choice)->{
                    WispState.set(this,"evolution_trait",WispEvolution.TRAITS[choice]);
                    WorldJournal.add(this,"You helped Wisp choose "+WispEvolution.TRAITS[choice]+" markings.");
                    render();
                }).show());
        button("Growth & appearance timeline",()->{
            String[] actions=WispEvolution.held(this)?
                new String[]{"Restore the previous appearance","Allow Wisp to evolve again","Keep this appearance"}:
                new String[]{"Restore the previous appearance","Pause automatic evolution","Keep growing"};
            new AlertDialog.Builder(this).setTitle(WispEvolution.description(this))
                .setItems(actions,(d,which)->{
                    if(which==0){
                        if(!WispEvolution.undo(this))
                            new AlertDialog.Builder(this).setMessage("Wisp has no previous transformation yet.")
                                .setPositiveButton("Okay",null).show();
                    }else if(which==1){
                        if(WispEvolution.held(this))WispEvolution.resume(this);
                        else WispState.prefs(this).edit().putBoolean("evolution_hold",true).apply();
                    }
                    render();
                }).setNegativeButton("Close",null).show();
        });
        button("Choose Wisp's name",()->{
            EditText edit=new EditText(this);edit.setSingleLine(true);edit.setText(WispState.name(this));
            new AlertDialog.Builder(this).setTitle("Your companion's name").setView(edit)
              .setPositiveButton("Save",(d,i)->{String n=edit.getText().toString().trim();if(!n.isEmpty())WispState.set(this,"name",n);render();})
              .setNegativeButton("Cancel",null).show();
        });
        text("WEATHER, SEASONS & PRIVACY",18);
        button("Save, restore or erase my world",()->
            startActivity(new Intent(this,SaveActivity.class)));
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
        text("Seasonal activities now have visible keepsakes in the cottage. Tap Explore → Inside cottage → Explore nearby to participate. Hanukkah is optional and its dates aren't guessed from the calendar.",12);
        text("This is still a development preview: walking is tap-to-move within each scene, with scene navigation rather than a continuous roaming map. Hand-painted art is a future milestone.",12);
    }
    private boolean prefsMet(){
        return WispState.prefs(this).getBoolean("matching_complete",false);
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(request,permissions,grants);
        if(request==7 && grants.length>0 && grants[0]==PackageManager.PERMISSION_GRANTED)WeatherClient.refresh(this);
    }
    @Override protected void onResume(){
        super.onResume();
        ContextEngine.startVisible(this);
        WeatherClient.refresh(this);
        if(body!=null)render();
    }
    @Override protected void onPause(){
        ContextEngine.stopVisible(this);
        super.onPause();
    }
}
