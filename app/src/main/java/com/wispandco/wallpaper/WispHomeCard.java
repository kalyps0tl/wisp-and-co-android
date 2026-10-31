package com.wispandco.wallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.View;
import java.util.Calendar;

/** Lightweight atmospheric overview card, no downloaded imagery or animation. */
public final class WispHomeCard extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public WispHomeCard(Context context){
        super(context);
        setContentDescription("Illustration of Wisp's cottage and pond. Double tap to explore.");
        setFocusable(true);
        setClickable(true);
    }
    private void oval(Canvas c,int color,float l,float t,float w,float h){
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);
        c.drawOval(l,t,l+w,t+h,p);
    }
    private void round(Canvas c,int color,float l,float t,float w,float h,float r){
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);
        c.drawRoundRect(l,t,l+w,t+h,r,r,p);
    }
    private void line(Canvas c,int color,float width,float x,float y,float xx,float yy){
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(width);
        c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);
    }
    private void poly(Canvas c,int color,float...points){
        p.setShader(null);p.setColor(color);
        Path path=new Path();path.moveTo(points[0],points[1]);
        for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);
        path.close();c.drawPath(path,p);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        c.save();c.scale(w/400f,h/260f);
        boolean night=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<7||
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=19;
        String wx=WispState.weather(getContext());
        int top=night?0xff5a5274:0xffc3c9d9,bottom=night?0xffa58ca3:0xffffe8d4;
        if("storm".equals(wx)){top=0xff7f7b90;bottom=0xffcac5d4;}
        p.setShader(new LinearGradient(0,0,0,260,top,bottom,Shader.TileMode.CLAMP));
        c.drawRoundRect(0,0,400,260,22,22,p);p.setShader(null);
        if(night){
            oval(c,0xfff9e9d1,312,29,22,22);
            oval(c,top,320,22,22,22);
            for(int i=0;i<15;i++)oval(c,0xccf9e9cf,(i*69)%385+10,(i*31)%97+8,2.5f,2.5f);
        }else oval(c,0x77fff0ce,314,21,51,51);
        poly(c,0xffa7adb4,-25,161,76,86,158,157);
        poly(c,0xffbdc1bf,119,157,227,92,352,167);
        poly(c,0xff94aca0,292,160,376,108,448,168);
        oval(c,0xff9aaba2,-20,144,455,76);
        // The pond is soft and reflective; ripple strokes suggest depth.
        oval(c,0xff85aab0,88,179,296,76);
        oval(c,0xffa7c4bc,104,191,258,45);
        for(int i=0;i<6;i++)line(c,0x77f8f2df,2,122+i*42,213+i%2*11,150+i*42,213+i%2*11);
        // Layered pine silhouettes.
        for(int i=0;i<8;i++){
            float x=i*56-9,y=163+i%2*12;
            poly(c,i%2==0?0xff6d938a:0xff809c8b,x-20,y,x+4,y-82-i%3*10,x+28,y);
            poly(c,0xff88a698,x-16,y-30,x+4,y-103-i%3*7,x+25,y-31);
        }
        // Little cottage: chimney, roof, window and warm door.
        round(c,0xffd9c4bf,37,111,119,88,5);
        round(c,0xff998692,119,71,17,39,3);
        poly(c,0xff8f788a,18,118,97,64,175,118);
        poly(c,0xffb5a0aa,35,112,97,76,158,112);
        round(c,0xfff8e4ae,54,137,30,28,4);
        line(c,0xffbca1a3,3,69,138,69,165);
        line(c,0xffbca1a3,3,54,151,84,151);
        round(c,0xff826e80,108,142,31,57,12);
        oval(c,0xffffe6ad,129,171,5,5);
        // A tree-stump lantern beside the path.
        round(c,0xff917c7d,192,179,13,38,3);
        oval(c,0xfff7d6a5,190,161,18,22);
        oval(c,0x66fff0ca,183,156,32,35);
        // Wisp with feet, arms and ears. The card is illustrative, not its full rig.
        c.save();c.translate(284,155);
        String form=WispState.spirit(getContext());
        if("antlered".equals(form)){
            line(c,0xffaf998d,4,-14,-25,-23,-53);
            line(c,0xffaf998d,4,14,-25,23,-53);
            line(c,0xffaf998d,3,-20,-43,-32,-48);
            line(c,0xffaf998d,3,20,-43,32,-48);
        }else if("moth".equals(form)){
            oval(c,0x99dbcbe7,-47,-20,31,53);
            oval(c,0x99e9d8ed,18,-20,31,53);
        }else if("sprout".equals(form)){
            line(c,0xff78a18a,4,0,-29,0,-56);
            oval(c,0xff8cb09b,-24,-64,25,11);
            oval(c,0xffb6cfad,0,-64,26,13);
        }else{
            poly(c,0xffdbc6e6,-29,-20,-34,-55,-4,-30);
            poly(c,0xffdbc6e6,29,-20,34,-55,4,-30);
        }
        oval(c,0xffe6d2ea,-40,-34,80,77);
        oval(c,0xfff8eaf2,-30,-27,60,63);
        oval(c,0xffd9c2e3,-49,1,22,28);
        oval(c,0xffd9c2e3,27,1,22,28);
        oval(c,0xffd8c3e5,-29,35,24,18);
        oval(c,0xffd8c3e5,8,35,24,18);
        oval(c,0xff9781aa,-17,-5,10,14);
        oval(c,0xff9781aa,8,-5,10,14);
        oval(c,0xffefbdc9,-23,15,14,7);
        oval(c,0xffefbdc9,10,15,14,7);
        line(c,0xffab91b3,2,-3,19,3,19);
        c.restore();
        // Warm foreground plants.
        for(int i=0;i<8;i++){
            float x=i*58-7;
            line(c,0xff638b7d,2,x,261,x+6,237-i%3*4);
            oval(c,i%2==0?0xffdcb3c8:0xffe9c8a6,x+1,235-i%3*4,11,7);
        }
        c.restore();
    }
}
