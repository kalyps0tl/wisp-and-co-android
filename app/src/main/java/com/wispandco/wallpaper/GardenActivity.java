package com.wispandco.wallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Three real-time storybook garden beds: sow, watch, admire and replant. */
public final class GardenActivity extends Activity {
    private LinearLayout body;
    private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);render();}
    private void text(String value,int size){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);
        t.setTextColor(0xff554c62);t.setPadding(0,dp(7),0,dp(9));
        body.addView(t);
    }
    private void button(String name,Runnable action){
        Button b=new Button(this);b.setText(name);b.setTextColor(0xff544a5d);
        b.setAllCaps(false);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0xfff2dbe6,0xffd9e7d8});bg.setCornerRadius(dp(17));
        b.setBackground(bg);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));
        lp.bottomMargin=dp(10);body.addView(b,lp);b.setOnClickListener(v->action.run());
    }
    private void render(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xfffbf7fa);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(20),dp(25),dp(20),dp(32));scroll.addView(body);
        setContentView(scroll);
        text("✦ The little cottage garden",25);
        text("Plants quietly grow with the passing days, even when your phone is off. They never wilt or die from being neglected.",15);
        View illustration=new GardenDrawing();
        LinearLayout.LayoutParams picture=new LinearLayout.LayoutParams(-1,dp(250));
        picture.bottomMargin=dp(17);body.addView(illustration,picture);
        text(GardenPatch.overview(this),15);
        for(int i=0;i<3;i++){
            final int bed=i;
            text("Bed "+(i+1)+" · "+GardenPatch.description(this,i),14);
            button(GardenPatch.kind(this,i)<0?"Plant a seed in bed "+(i+1):
                GardenPatch.phase(this,i)>=3?"Admire or replant "+GardenPatch.NAMES[GardenPatch.kind(this,i)]:
                    "Check on "+GardenPatch.NAMES[GardenPatch.kind(this,i)],()->choose(bed));
        }
        text("Different plants bloom at their own pace, usually after three to five calendar days. You can always change a bed or keep your flowers.",13);
    }
    private void choose(int bed){
        int current=GardenPatch.kind(this,bed);
        if(current<0){
            new AlertDialog.Builder(this).setTitle("What shall we grow?")
                .setItems(GardenPatch.NAMES,(d,i)->{
                    GardenPatch.plant(this,bed,i);render();
                }).setNegativeButton("Not today",null).show();
        }else{
            String label=GardenPatch.NAMES[current];
            new AlertDialog.Builder(this).setTitle(label)
                .setMessage(GardenPatch.description(this,bed))
                .setPositiveButton("Admire together",(d,i)->{
                    GardenPatch.admire(this,bed);
                    new AlertDialog.Builder(this).setTitle(WispState.name(this)+" says")
                        .setMessage(WispState.note(this)).setPositiveButton("♥",null).show();
                    render();
                }).setNeutralButton("Plant something new",(d,i)->
                    new AlertDialog.Builder(this).setTitle("Which seed?")
                        .setItems(GardenPatch.NAMES,(dd,choice)->{
                            GardenPatch.plant(this,bed,choice);render();
                        }).show())
                .setNegativeButton("Keep growing",null).show();
        }
    }
    private final class GardenDrawing extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        GardenDrawing(){super(GardenActivity.this);
            setContentDescription("Three illustrated garden beds. Tap any of the labeled buttons below to manage plants.");}
        private void oval(Canvas c,int color,float x,float y,float w,float h){
            p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.FILL);
            c.drawOval(x,y,x+w,y+h,p);
        }
        private void line(Canvas c,int color,float width,float x,float y,float xx,float yy){
            p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);
        }
        private void triangle(Canvas c,int color,float x,float y,float x2,float y2,float x3,float y3){
            Path path=new Path();path.moveTo(x,y);path.lineTo(x2,y2);path.lineTo(x3,y3);
            path.close();p.setColor(color);p.setShader(null);c.drawPath(path,p);
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            c.save();c.scale(getWidth()/360f,getHeight()/240f);
            p.setShader(new LinearGradient(0,0,0,240,0xffe1d8e6,0xffebead7,Shader.TileMode.CLAMP));
            c.drawRoundRect(0,0,360,240,20,20,p);p.setShader(null);
            oval(c,0x77f7e8ca,269,20,40,40);
            oval(c,0xffa4c1ab,-20,120,395,141);
            for(int i=0;i<3;i++){
                float x=39+i*103,y=177;
                oval(c,0xff987e76,x-12,y-5,95,24);
                oval(c,0xff715f65,x-9,y,89,15);
                int stage=GardenPatch.phase(GardenActivity.this,i);
                if(stage==0){
                    oval(c,0xffd9bc98,x+37,y-11,6,5);continue;
                }
                if(stage<0)continue;
                float size=stage==1?25:stage==2?61:97;
                line(c,0xff719981,4,x+37,y,x+37,y-size);
                if(stage>=1){
                    oval(c,0xff8fb79a,x+13,y-size*.55f,25,13);
                    oval(c,0xffbad1a1,x+38,y-size*.75f,27,14);
                }
                if(stage>=2){
                    oval(c,0xff86ae93,x+19,y-size*.87f,20,12);
                    oval(c,0xffa2c7aa,x+37,y-size*.95f,24,11);
                }
                if(stage==3){
                    int kind=GardenPatch.kind(GardenActivity.this,i);
                    int color=new int[]{0xffeee2d8,0xffd9aa85,0xffd5b8dc,0xffad93c4,0xffbad7b2}[kind];
                    for(int j=0;j<6;j++){
                        double a=j*Math.PI/3;
                        oval(c,color,x+33+(float)Math.cos(a)*13,y-size-4+(float)Math.sin(a)*13,13,13);
                    }
                    oval(c,0xfff1d9a3,x+34,y-size-3,14,14);
                }
            }
            for(int i=0;i<12;i++){
                float x=i*33,y=211+(i%3)*6;
                line(c,0xff659583,2,x,y+15,x+4,y-3);
                oval(c,0xffd8b2c9,x,y-9,8,7);
            }
            c.restore();
        }
    }
}
