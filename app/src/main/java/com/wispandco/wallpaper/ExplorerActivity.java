package com.wispandco.wallpaper;

import android.app.Activity;
import android.app.AlertDialog;
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
        Button friend=new Button(this);
        friend.setText("Visit a woodland neighbor  ♥");
        friend.setAllCaps(false);
        top.addView(friend);
        friend.setOnClickListener(v->visitNeighbor());
        Button paths=new Button(this);paths.setText("Follow a path…");paths.setAllCaps(false);
        top.addView(paths);paths.setOnClickListener(v->showPaths());
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
            ".  Your world: "+ContextEngine.dayPart()+", "+ContextEngine.motion(this)+
            ". Tap glimmers to interact, or tap the ground to walk.");
        scene.invalidate();
    }
    private void travel(String destination){
        if(!SceneRoutes.connects(activeZone,destination))return;
        activeZone=destination;
        WispState.set(this,"zone",destination);
        WispState.prefs(this).edit()
            .putString("note","A path led us to the "+SceneRoutes.title(destination).toLowerCase()+".")
            .apply();
        update();
    }
    private void showPaths(){
        String[] possible=SceneRoutes.paths(activeZone);
        String[] options=new String[possible.length];
        for(int i=0;i<possible.length;i++)options[i]="Take the path to the "+SceneRoutes.title(possible[i]);
        new AlertDialog.Builder(this).setTitle("Where does the trail lead?")
            .setItems(options,(d,i)->travel(possible[i]))
            .setNegativeButton("Stay here",null).show();
    }
    private void visitNeighbor(){
        int n=WorldNeighbors.index(activeZone);
        if(n<0){
            new AlertDialog.Builder(this).setTitle("Down by the dock")
                .setMessage("Pip the frog usually lives near the lily pads. Would you like to visit the pond?")
                .setPositiveButton("Visit pond",(d,i)->{
                    activeZone="pond";WispState.set(this,"zone","pond");update();visitNeighbor();
                }).setNegativeButton("Not now",null).show();
            return;
        }
        String name=WorldNeighbors.name(n);
        new AlertDialog.Builder(this).setTitle(name)
            .setMessage(WorldNeighbors.schedule(this,n)+"\n\n"+WorldNeighbors.friendship(this,n)+
                " · "+WorldNeighbors.meetings(this,n)+" visits\n\n"+WorldNeighbors.FAVORITES[n]+
                "\n\nFriendships grow gently with time, without streaks or penalties.")
            .setPositiveButton("Say hello",(d,i)->{
                String story=WorldNeighbors.visit(this,n);
                new AlertDialog.Builder(this).setTitle("A moment together")
                    .setMessage(story)
                    .setPositiveButton("How lovely",null).show();
                update();
            }).setNegativeButton("Let them be",null).show();
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
        private final Paint marker=new Paint(Paint.ANTI_ALIAS_FLAG);
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
            // Visible path exits, using the same centered aspect-fill transform
            // as the world renderer. Quick area buttons remain available too.
            float scale=Math.max(getWidth()/400f,getHeight()/900f);
            float tx=(getWidth()-400f*scale)/2f,ty=(getHeight()-900f*scale)/2f;
            canvas.save();canvas.translate(tx,ty);canvas.scale(scale,scale);
            // Subtle affordances so a user can discover objects without a
            // cluttered marker or a bright game-like HUD.
            for(WorldHotspots.Point h:WorldHotspots.forZone(activeZone)){
                marker.setStyle(Paint.Style.FILL);
                marker.setColor(0x66fff0d9);
                canvas.drawCircle(h.x,h.y,11,marker);
                marker.setColor(0xaafff9e5);
                canvas.drawCircle(h.x,h.y,4,marker);
                marker.setColor(0x889f819e);
                marker.setStyle(Paint.Style.STROKE);marker.setStrokeWidth(1.6f);
                canvas.drawCircle(h.x,h.y,13,marker);
                marker.setStyle(Paint.Style.FILL);
            }
            marker.setColor(0x99786b80);
            canvas.drawRoundRect(2,650,86,713,18,18,marker);
            canvas.drawRoundRect(314,650,398,713,18,18,marker);
            marker.setColor(0xfffff1e6);marker.setTextSize(13);
            marker.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("←",43,674,marker);
            canvas.drawText(SceneRoutes.title(SceneRoutes.left(activeZone)),43,692,marker);
            canvas.drawText("→",357,674,marker);
            canvas.drawText(SceneRoutes.title(SceneRoutes.right(activeZone)),357,692,marker);
            canvas.restore();
            if(animating && getVisibility()==VISIBLE)postInvalidateDelayed(ContextEngine.reducedMotion(ExplorerActivity.this)?110:50);
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            if(event.getAction()==MotionEvent.ACTION_UP){
                float scale=Math.max(getWidth()/400f,getHeight()/900f);
                float x=(event.getX()-(getWidth()-400f*scale)/2f)/scale;
                float y=(event.getY()-(getHeight()-900f*scale)/2f)/scale;
                if(y>=650 && y<=713 && x<=86){
                    travel(SceneRoutes.left(activeZone));return true;
                }
                if(y>=650 && y<=713 && x>=314){
                    travel(SceneRoutes.right(activeZone));return true;
                }
                WorldHotspots.Point found=WorldHotspots.find(activeZone,x,y);
                if(found!=null){
                    if("cottage".equals(found.kind) && SceneRoutes.connects(activeZone,"cottage")){
                        travel("cottage");
                    }else{
                        String message=WorldHotspots.interact(ExplorerActivity.this,found);
                        new AlertDialog.Builder(ExplorerActivity.this)
                            .setTitle(found.label)
                            .setMessage(message)
                            .setPositiveButton("Lovely",null).show();
                        update();
                    }
                    return true;
                }
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
    @Override protected void onResume(){
        super.onResume();
        animating=true;
        ContextEngine.startVisible(this);
        WeatherClient.refresh(this);
        update();
    }
    @Override protected void onPause(){
        animating=false;
        ContextEngine.stopVisible(this);
        super.onPause();
    }
    @Override protected void onDestroy(){if(scene!=null)scene.renderer.release();super.onDestroy();}
}
