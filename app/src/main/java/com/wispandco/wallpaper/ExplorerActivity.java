package com.wispandco.wallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The full companion explorer. Tap the ground to walk. Inspect objects, make
 * things and meet creatures without turning Wisp into a chores-for-coins pet.
 */
public final class ExplorerActivity extends Activity {
    private final String[] zones={"dock","garden","pond","forest","cottage"};
    private final String[] names={"Dock","Garden","Pond","Forest","Inside cottage"};
    private final String[][] actions={
        {"Collect a smooth stone","Watch the water","See Wisp's creation"},
        {"Visit the bees","Plant a tiny flower","Look at Wisp's garden"},
        {"Greet the frog","Follow a dragonfly","Listen to the reeds"},
        {"Follow a hidden trail","Find a mushroom","Rest under the trees"},
        {"Read our journal","Work on a project","Celebrate a season"}
    };
    private SceneView scene;
    private TextView caption;
    private TextView heading;
    private boolean animating;
    private String activeZone="dock";
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        activeZone=WispState.zone(this);
        FrameLayout root=new FrameLayout(this);
        scene=new SceneView();root.addView(scene,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout top=new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(16),dp(22),dp(16),dp(12));
        top.setBackgroundColor(0x99978ba7);
        heading=new TextView(this);
        heading.setTextColor(0xffffffff);heading.setTextSize(21);
        top.addView(heading);
        caption=new TextView(this);
        caption.setTextColor(0xfffaf6f7);caption.setTextSize(13);
        caption.setMaxLines(3);
        top.addView(caption);
        Button action=new Button(this);action.setText("Explore nearby ✦");action.setAllCaps(false);
        top.addView(action);action.setOnClickListener(v->showActions());
        root.addView(top,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));

        HorizontalScrollView strip=new HorizontalScrollView(this);
        strip.setHorizontalScrollBarEnabled(false);
        strip.setBackgroundColor(0xdd988ca4);
        LinearLayout row=new LinearLayout(this);
        row.setPadding(dp(5),dp(5),dp(5),dp(5));
        for(int i=0;i<zones.length;i++){
            final String zone=zones[i];
            Button bZone=new Button(this);bZone.setAllCaps(false);bZone.setText(names[i]);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(50));
            lp.setMargins(dp(2),0,dp(2),0);row.addView(bZone,lp);
            bZone.setOnClickListener(v->{activeZone=zone;WispState.set(this,"zone",zone);update();});
        }
        strip.addView(row);
        root.addView(strip,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
        setContentView(root);
        update();
    }
    private int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+.5f);}
    private int index(){
        for(int i=0;i<zones.length;i++)if(zones[i].equals(activeZone))return i;
        return 0;
    }
    private void update(){
        WispState.advance(this);
        heading.setText("✦ "+names[index()]+"  ·  "+WispState.weather(this));
        caption.setText(WispState.name(this)+" is "+WispState.activity(this)+
            ".  Tap the ground to walk, or explore nearby.");
        scene.invalidate();
    }
    private void showActions(){
        new AlertDialog.Builder(this).setTitle("What shall we do here?")
            .setItems(actions[index()],(dialog,choice)->perform(choice)).show();
    }
    private void perform(int choice){
        if("cottage".equals(activeZone)){
            if(choice==0){
                new AlertDialog.Builder(this).setTitle("Wisp's journal")
                    .setMessage(WorldJournal.recent(this))
                    .setPositiveButton("Back",null).show();return;
            }
            if(choice==1){
                WispState.prefs(this).edit().putInt("projectProgress",
                    Math.min(3,WispState.projectProgress(this)+1))
                    .putString("note","I like making things together! Look at our little workbench.").apply();
                WorldJournal.add(this,"Helped Wisp with "+WispState.currentProject(this)+".");
                WispEvolution.discover(this,"workbench");update();return;
            }
            celebrations();return;
        }
        String[][] encounters={
            {"a mysterious pebble","the sound of reeds and water","a tiny handmade keepsake"},
            {"the visiting honeybee","a new garden flower","the fragrant flower bed"},
            {"our little frog neighbor","a translucent dragonfly","the rustling reeds"},
            {"an old woodland trail","a spotted mushroom","a mossy resting place"}
        };
        String found=encounters[index()][choice];
        WispEvolution.discover(this,found);
        WorldJournal.add(this,"Together, we discovered "+found+".");
        scene.renderer.setDestination(90+choice*107);
        update();
    }
    private void celebrations(){
        String[] choices=new String[3];
        for(int i=0;i<3;i++)
            choices[i]=(WorldEvents.selected(this,i)?"":"(Not selected) ")+WorldEvents.status(this,i);
        new AlertDialog.Builder(this).setTitle("A season in Wisp's cottage")
            .setItems(choices,(d,which)->{
                if(!WorldEvents.selected(this,which)){
                    new AlertDialog.Builder(this).setTitle("An optional tradition")
                        .setMessage("Enable this celebration in the companion settings first. Wisp celebrates only the traditions you choose.")
                        .setPositiveButton("Okay",null).show();return;
                }
                WorldEvents.takePart(this,which);update();
            }).setNegativeButton("Not today",null).show();
    }
    private final class SceneView extends View {
        private final WorldRenderer renderer=new WorldRenderer();
        SceneView(){super(ExplorerActivity.this);}
        @Override protected void onDraw(Canvas canvas){
            renderer.draw(canvas,getWidth(),getHeight(),activeZone,
                WispState.weather(ExplorerActivity.this),
                WispState.spirit(ExplorerActivity.this),
                System.currentTimeMillis(),WispEvolution.stage(ExplorerActivity.this),
                WispEvolution.trait(ExplorerActivity.this),
                WorldEvents.selected(ExplorerActivity.this,0)?WorldEvents.progress(ExplorerActivity.this,0):0,
                WorldEvents.selected(ExplorerActivity.this,1)?WorldEvents.progress(ExplorerActivity.this,1):0,
                WorldEvents.selected(ExplorerActivity.this,2)?WorldEvents.progress(ExplorerActivity.this,2):0);
            if(animating && getVisibility()==VISIBLE)postInvalidateDelayed(50);
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            if(event.getAction()==MotionEvent.ACTION_UP){
                float x=event.getX()/Math.max(1,getWidth())*400f;
                float y=event.getY()/Math.max(1,getHeight())*900f;
                if(y>225 && y<830){
                    renderer.setDestination(x);
                    WispState.prefs(ExplorerActivity.this).edit()
                        .putString("note","Let's look over there together!").apply();
                    invalidate();
                }
                return true;
            }
            return true;
        }
    }
    @Override protected void onResume(){super.onResume();animating=true;WeatherClient.refresh(this);update();}
    @Override protected void onPause(){animating=false;super.onPause();}
    @Override protected void onDestroy(){if(scene!=null)scene.renderer.release();super.onDestroy();}
}
