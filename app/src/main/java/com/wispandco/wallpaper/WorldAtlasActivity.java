package com.wispandco.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** A native illustrated five-stop atlas; never requests mapping or GPS data. */
public final class WorldAtlasActivity extends Activity {
    private static final String[] ZONES={"forest","pond","dock","garden","cottage"};
    private static final String[] NAMES={"Whispering Forest","Lily Pond","Lantern Dock","Wildflower Garden","Wisp's Cottage"};
    private static final String[] HINTS={
        "Alder likes the moon and secret woodland stories",
        "Pip waits where the lily pads meet the reeds",
        "The river carries old stories beyond the mountain",
        "Bramble visits the blossoms when the sun is warm",
        "Fenn watches over the books by the hearth"
    };
    private static final int[] MAP_X={91,290,184,302,139};
    private static final int[] MAP_Y={133,167,285,390,487};
    private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(0xfff9f4f7);
        LinearLayout body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(16),dp(20),dp(16),dp(36));
        sc.addView(body);
        TextView title=new TextView(this);
        title.setText("✦  The Atlas of Little Places");title.setTextSize(24);
        title.setTextColor(0xff5e536c);title.setPadding(dp(5),0,dp(5),dp(8));
        body.addView(title);
        TextView hint=new TextView(this);
        hint.setText("A world connected by footpaths. Choose a place to visit Wisp and its neighbors.");
        hint.setTextColor(0xff665d6b);hint.setTextSize(14);
        hint.setPadding(dp(5),0,dp(5),dp(12));body.addView(hint);
        WorldMap map=new WorldMap();
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(530));
        lp.bottomMargin=dp(18);
        body.addView(map,lp);
        for(int i=0;i<ZONES.length;i++){
            final int n=i;
            Button bZone=new Button(this);
            bZone.setAllCaps(false);bZone.setText(NAMES[i]+"  ·  "+discoveryCount(ZONES[i])+" memories");
            bZone.setTextColor(0xff51465c);
            GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xffe9dce9,0xffdce8dd});bg.setCornerRadius(dp(15));bZone.setBackground(bg);
            LinearLayout.LayoutParams row=new LinearLayout.LayoutParams(-1,dp(55));
            row.bottomMargin=dp(9);body.addView(bZone,row);
            bZone.setOnClickListener(v->visit(n));
        }
        setContentView(sc);
    }
    private int discoveryCount(String zone){
        int found=0;
        for(WorldHotspots.Point point:WorldHotspots.forZone(zone))
            if(WispState.prefs(this).getLong("hotspot_"+point.id+"_last",0)>0)found++;
        return found;
    }
    private void visit(int index){
        WispState.set(this,"zone",ZONES[index]);
        Intent i=new Intent(this,ExplorerActivity.class);
        startActivity(i);
        finish();
    }
    private final class WorldMap extends View {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        WorldMap(){super(WorldAtlasActivity.this);
            setContentDescription("Illustrated world map with the forest, pond, dock, garden and cottage. Choose a place using the named buttons underneath.");
        }
        void circle(Canvas c,int color,float x,float y,float r){
            paint.setColor(color);paint.setShader(null);paint.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,paint);
        }
        void poly(Canvas c,int color,float...coords){
            paint.setColor(color);paint.setShader(null);Path p=new Path();p.moveTo(coords[0],coords[1]);
            for(int i=2;i<coords.length;i+=2)p.lineTo(coords[i],coords[i+1]);p.close();c.drawPath(p,paint);
        }
        void text(Canvas c,String value,float x,float y,int size){
            paint.setColor(0xff5d5668);paint.setShader(null);paint.setTextSize(size);
            paint.setTypeface(android.graphics.Typeface.create("serif",android.graphics.Typeface.NORMAL));
            paint.setTextAlign(Paint.Align.CENTER);c.drawText(value,x,y,paint);
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);c.save();c.scale(getWidth()/400f,getHeight()/570f);
            paint.setShader(new LinearGradient(0,0,400,570,0xffe5e1d7,0xffe7cddd,Shader.TileMode.CLAMP));
            c.drawRoundRect(0,0,400,570,22,22,paint);paint.setShader(null);
            for(int j=0;j<25;j++){
                float x=(j*89)%402, y=(j*47)%570;
                circle(c,0x88a8afa0,x,y,2.5f);
            }
            poly(c,0xffb8c2b6,-20,120,94,32,190,140);
            poly(c,0xffc2b7bd,123,110,258,34,427,154);
            // A winding stream meets the pond.
            paint.setColor(0xff93b6b8);paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(23);paint.setStrokeCap(Paint.Cap.ROUND);
            Path river=new Path();river.moveTo(306,103);river.cubicTo(243,153,327,227,189,286);
            river.cubicTo(112,333,274,410,183,555);c.drawPath(river,paint);
            paint.setStyle(Paint.Style.FILL);
            // Deliberately linked routes, as a visual representation of SceneRoutes.
            for(String from:SceneRoutes.ZONES){
                int a=position(from);
                for(String to:SceneRoutes.paths(from)){
                    int b=position(to);
                    if(a>=b)continue;
                    paint.setColor(0xffb9a28f);paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(6);paint.setPathEffect(
                        new android.graphics.DashPathEffect(new float[]{5,10},0));
                    c.drawLine(MAP_X[a],MAP_Y[a],MAP_X[b],MAP_Y[b],paint);
                    paint.setPathEffect(null);paint.setStyle(Paint.Style.FILL);
                }
            }
            for(int i=0;i<ZONES.length;i++){
                float x=MAP_X[i],y=MAP_Y[i];
                circle(c,0x66fff3da,x,y,32);
                circle(c,0xfff5e7da,x,y,24);
                circle(c,0xffa892a8,x,y,20);
                if(i==0){ // Forest tree
                    poly(c,0xffb6d0b7,x-14,y+9,x,y-18,x+14,y+9);
                    poly(c,0xff7f9d90,x-10,y-2,x,y-23,x+10,y-2);
                }else if(i==1){ // Lily
                    circle(c,0xff8fb1a2,x,y,14);
                    circle(c,0xffeecbd8,x,y-2,6);
                }else if(i==2){ // Lantern
                    circle(c,0xffffe6a9,x,y,10);
                    paint.setColor(0xff735f6b);paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);c.drawRect(x-11,y-13,x+11,y+14,paint);
                    paint.setStyle(Paint.Style.FILL);
                }else if(i==3){ // Flowers
                    for(int p=0;p<5;p++){
                        float a=(float)(p*6.283185/5);
                        circle(c,0xffe9c9d8,x+(float)Math.sin(a)*9,
                            y+(float)Math.cos(a)*9,7);
                    }
                    circle(c,0xfff6e2a8,x,y,5);
                }else{ // Cottage
                    poly(c,0xfff4e0d8,x-15,y-1,x,y-19,x+15,y-1);
                    paint.setColor(0xffe5c2b5);
                    c.drawRect(x-12,y-2,x+12,y+14,paint);
                    circle(c,0xffffe7b1,x+5,y+6,3);
                }
                paint.setColor(0xfff7eedf);paint.setTextSize(12);
                text(c,new String[]{"Forest","Pond","Dock","Garden","Cottage"}[i],x,y+47,14);
            }
            text(c,"A map of small wonders",200,552,17);
            c.restore();
        }
        private int position(String zone){
            for(int i=0;i<ZONES.length;i++)if(ZONES[i].equals(zone))return i;
            return -1;
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_UP){
                float x=e.getX()/Math.max(1,getWidth())*400;
                float y=e.getY()/Math.max(1,getHeight())*570;
                int best=-1;float dist=1800;
                for(int i=0;i<ZONES.length;i++){
                    float dx=x-MAP_X[i],dy=y-MAP_Y[i],d=dx*dx+dy*dy;
                    if(d<dist){best=i;dist=d;}
                }
                if(best>=0)visit(best);
                return true;
            }
            return true;
        }
    }
}
