package com.wispandco.wallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import java.util.Calendar;
import java.util.Random;

/** Independent native scene renderer. All drawing uses a fixed 400x900 design canvas. */
public final class WorldRenderer {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r=new RectF();
    private float time;
    private String weather;
    private void fill(Canvas c,int color,float x,float y,float w,float h){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(x,y,x+w,y+h,p);}
    private void oval(Canvas c,int color,float x,float y,float w,float h){p.setColor(color);p.setShader(null);c.drawOval(x,y,x+w,y+h,p);}
    private void round(Canvas c,int color,float x,float y,float w,float h,float radius){
        p.setColor(color);p.setShader(null);r.set(x,y,x+w,y+h);c.drawRoundRect(r,radius,radius,p);
    }
    private void line(Canvas c,int color,float stroke,float x1,float y1,float x2,float y2){
        p.setColor(color);p.setShader(null);p.setStrokeWidth(stroke);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);
    }
    private void triangle(Canvas c,int color,float ax,float ay,float bx,float by,float cx,float cy){
        p.setColor(color);p.setShader(null);Path t=new Path();t.moveTo(ax,ay);t.lineTo(bx,by);t.lineTo(cx,cy);t.close();c.drawPath(t,p);
    }
    private void circle(Canvas c,int color,float x,float y,float rad){p.setColor(color);p.setShader(null);c.drawCircle(x,y,rad,p);}
    public void draw(Canvas c,int w,int h,String zone,String weather,String spirit,long timestamp){
        if(w<=0||h<=0)return;
        c.save();c.scale(w/400f,h/900f);
        time=(timestamp%1000000L)/1000f;this.weather=weather;
        boolean night=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=19 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<6;
        sky(c,night);
        if("cottage".equals(zone))interior(c);
        else {
            mountains(c);
            if("forest".equals(zone)) forest(c);
            else if("garden".equals(zone))garden(c);
            else if("pond".equals(zone))pond(c);
            else dock(c);
        }
        wildlife(c,zone);
        wisp(c,spirit,zone);
        atmospheric(c,weather);
        c.restore();
    }
    private void sky(Canvas c,boolean night){
        int top=night?0xff403952:0xffd4c3e3,bottom=night?0xff9e89ae:0xffffe1db;
        if("storm".equals(weather)){top=0xff696a83;bottom=0xffc0b4ca;}
        p.setShader(new LinearGradient(0,0,0,900,top,bottom,Shader.TileMode.CLAMP));
        c.drawRect(0,0,400,900,p);p.setShader(null);
        if(night) {
            circle(c,0xffffeecf,315,135,21);circle(c,0xff403952,326,124,20);
            for(int i=0;i<20;i++){float x=(i*73)%380+10,y=(i*47)%310+30;circle(c,0x77fff8df,x,y,1.8f);}
        }else {circle(c,0x88fff2d0,309,145,40);}
        for(int i=0;i<3;i++){
            float dx=(float)Math.sin(time*.025+i)*8;
            oval(c,0x55ffffff,i*128f+24+dx,240+i*27,128,33);
        }
    }
    private void mountains(Canvas c){
        triangle(c,0xffbeb5ce,-70,555,90,365,255,555);
        triangle(c,0xffc7bfd7,112,545,270,405,445,545);
        triangle(c,0xff9eafae,-50,585,125,510,280,585);
        triangle(c,0xffa5b6ad,175,590,320,498,470,590);
        fill(c,0xff9eb7a3,0,575,400,325);
        for(int i=0;i<10;i++)pine(c,i*46-18,590+(i%3)*10,42+i%3*15);
    }
    private void pine(Canvas c,float x,float y,float size){
        line(c,0xff816971,3,x,y,x,y-size*.94f);
        triangle(c,0xff718f86,x-size*.45f,y-size*.23f,x,y-size,x+size*.45f,y-size*.23f);
        triangle(c,0xff8ba79a,x-size*.35f,y-size*.55f,x,y-size*1.08f,x+size*.35f,y-size*.55f);
    }
    private void cottage(Canvas c,float x,float y){
        oval(c,0x557e6575,x-19,y+116,170,27);
        round(c,0xffd8c1bb,x,y,132,127,6);
        triangle(c,0xff937c90,x-22,y+15,x+66,y-56,x+153,y+15);
        triangle(c,0xff796a89,x-12,y+7,x+66,y-45,x+144,y+7);
        round(c,0xff8d7483,x+50,y+66,34,61,16);
        circle(c,0xffffe5b8,x+77,y+98,3);
        round(c,0xffffedbb,x+14,y+42,27,32,4);
        round(c,0xffffedbb,x+96,y+42,23,32,4);
        line(c,0xffbc9e9f,3,x+27,y+43,x+27,y+73);
        line(c,0xffbc9e9f,3,x+14,y+57,x+42,y+57);
        for(int i=0;i<4;i++)circle(c,0xffd6b6d7,x+18+i*29,y+132+(i%2)*3,7);
    }
    private void flower(Canvas c,float x,float y,int color,float t){
        line(c,0xff638d73,2,x,y,x+(float)Math.sin(t+x)*3,y-15);
        for(int i=0;i<5;i++){
            double a=i*Math.PI*2/5;
            circle(c,color,x+(float)Math.cos(a)*5,y-18+(float)Math.sin(a)*5,4.2f);
        }
        circle(c,0xffffeab2,x,y-18,3);
    }
    private void dock(Canvas c){
        pondWater(c,630);
        cottage(c,47,455);
        flower(c,350,665,0xffecd2e2,time);
        flower(c,330,696,0xffefe3b4,time);
        fill(c,0xffa88d85,155,746,220,42);
        for(int i=0;i<7;i++)line(c,0xff8c7779,1,165+i*30,748,165+i*30,786);
        round(c,0xff96837e,186,780,11,65,3);
        round(c,0xff96837e,344,779,11,65,3);
    }
    private void pondWater(Canvas c,float y){
        p.setShader(new LinearGradient(0,y,0,900,0xff93aeb5,0xff6c969a,Shader.TileMode.CLAMP));
        c.drawRect(0,y,400,900,p);p.setShader(null);
        for(int i=0;i<16;i++){
            float yy=y+20+i*17,x=(i*77)%400;
            line(c,0x66e6e9dc,1.4f,x,yy,x+22+(float)Math.sin(time*1.1+i)*7,yy);
        }
        for(int i=0;i<5;i++){
            float xx=(i*94+24)%385,yy=y+35+(i%3)*63;
            oval(c,0xff739b83,xx,yy,26,11);
            oval(c,0xffb5cba7,xx+3,yy+2,16,5);
        }
        for(int i=0;i<3;i++)oval(c,0x44f8fffa,95+i*97,746+i*14,22+(float)Math.sin(time+i)*4,7);
    }
    private void pond(Canvas c){
        pondWater(c,585);
        for(int i=0;i<7;i++)flower(c,18+i*57,652+(i%3)*43,0xffe8d1e4,time);
        round(c,0xffb29a91,60,694,166,17,4);
        round(c,0xffa9908b,73,706,9,110,3);
        round(c,0xffa9908b,198,706,9,110,3);
        // Frog resting on a lily.
        oval(c,0xff638d73,288,742,59,19);
        oval(c,0xff9cbb87,305,716,35,33);
        circle(c,0xffe4eed0,314,718,8);circle(c,0xffe4eed0,330,718,8);
        circle(c,0xff3c5552,314,718,2.3f);circle(c,0xff3c5552,330,718,2.3f);
    }
    private void garden(Canvas c){
        fill(c,0xffa6bea0,0,605,400,295);
        cottage(c,218,461);
        round(c,0xffb69b83,16,732,230,87,11);
        round(c,0xff766b6f,23,738,218,12,4);
        for(int i=0;i<12;i++)flower(c,26+i*30,676+(i%4)*37,
            i%3==0?0xffeac4dc:(i%3==1?0xffffe0ad:0xffd7d5e9),time);
        for(int i=0;i<4;i++)circle(c,0xfff1dfb1,280+i*23,770+i%2*30,8);
    }
    private void forest(Canvas c){
        fill(c,0xff8da996,0,610,400,290);
        for(int i=0;i<7;i++){
            float x=i*73-30;
            line(c,0xff8d777b,11,x,785,x,328+(i%2)*65);
            for(int k=0;k<4;k++)oval(c,0xff819e8c,x-48+k*14,341+(i%2)*65+k*16,103,60);
        }
        Path path=new Path();path.moveTo(184,900);path.cubicTo(95,790,266,724,198,601);
        p.setColor(0xffc4bca6);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(57);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
        for(int i=0;i<8;i++){
            float x=(i*53)%380+12,y=670+(i%4)*42;
            oval(c,0xff9e859f,x,y,19,12);line(c,0xffede1be,4,x+10,y+9,x+10,y+20);
        }
    }
    private void interior(Canvas c){
        fill(c,0xffdbc5c4,0,0,400,900);
        fill(c,0xffbd9e98,0,632,400,268);
        for(int i=0;i<10;i++)line(c,0xffae8c8f,2,0,650+i*28,400,650+i*28);
        round(c,0xff866f82,54,240,100,247,8);
        for(int j=0;j<4;j++){
            line(c,0xff684f69,7,62,281+j*55,146,281+j*55);
            for(int k=0;k<6;k++)round(c,new int[]{0xffb5c3aa,0xffe9bfba,0xffa1afc4}[k%3],66+k*13,240+j*55,9,38,2);
        }
        round(c,0xff8a6c71,253,352,115,217,9);
        round(c,0xff5d4d59,265,390,89,166,42);
        oval(c,0xffffce92,279,438,64,88);
        triangle(c,0xffffe9a0,285,522,309,422,338,520);
        round(c,0xffb69b93,25,663,239,25,6);
        round(c,0xffa58a84,45,681,16,93,3);round(c,0xffa58a84,230,681,16,93,3);
        oval(c,0xfff4d6d7,95,620,107,55);
        oval(c,0xffb5a7cf,111,629,72,39);
        round(c,0xfff8e1ae,174,641,31,17,8);
        // Painted cottage window.
        round(c,0xff927c8e,188,212,110,125,7);
        round(c,0xffadc6ca,196,220,94,109,3);
        line(c,0xfff5e5cf,6,244,220,244,328);
        line(c,0xfff5e5cf,6,196,276,291,276);
    }
    private void wildlife(Canvas c,String zone){
        if("cottage".equals(zone)){
            for(int i=0;i<5;i++){
                float x=306+i*9,y=310+(float)Math.sin(time*1.2+i)*6;
                circle(c,0x99ffe9b0,x,y,2.1f);
            }
            return;
        }
        if(!"storm".equals(weather)&&!"snow".equals(weather)){
            for(int i=0;i<3;i++){
                float x=78+i*110+(float)Math.sin(time*1.3+i)*12;
                float y=610-i*37+(float)Math.cos(time*2+i)*12;
                oval(c,0x99fffaf1,x-9,y-5,12,8);oval(c,0x99fffaf1,x+2,y-5,12,8);
                oval(c,0xffc99d70,x-2,y-2,10,7);line(c,0xff5a5261,1,x+3,y-2,x+4,y+3);
            }
            for(int i=0;i<2;i++){
                float x=145+i*131+(float)Math.sin(time*1.5+i)*25;
                float y=500+i*70+(float)Math.cos(time*2+i)*13;
                oval(c,0x99b5dad6,x-14,y-11,17,10);oval(c,0x99d5e8e4,x+2,y-11,17,10);
                oval(c,0x99b5dad6,x-14,y+2,17,10);oval(c,0x99d5e8e4,x+2,y+2,17,10);
                line(c,0xff857c9d,3,x,y-13,x,y+14);
            }
        }
    }
    private void wisp(Canvas c,String shape,String zone){
        float y="cottage".equals(zone)?738:("pond".equals(zone)?695:("garden".equals(zone)?724:("forest".equals(zone)?716:705));
        float x="dock".equals(zone)?294:("garden".equals(zone)?177:("pond".equals(zone)?161:211);
        float bob=(float)Math.sin(time*2.2)*5;
        c.save();c.translate(x,y+bob);
        oval(c,0x447b6c89,-32,42,66,12);
        if("moth".equals(shape)){
            oval(c,0x99d9c9e7,-46,-21,36,48);oval(c,0x99e8d7ee,11,-21,36,48);
            line(c,0xffa790b8,3,-9,-28,-17,-54);line(c,0xffa790b8,3,9,-28,17,-54);
            circle(c,0xffc8a9c9,-17,-54,5);circle(c,0xffc8a9c9,17,-54,5);
        }else if("antlered".equals(shape)){
            line(c,0xffb49b91,5,-12,-23,-23,-72);line(c,0xffb49b91,5,-23,-56,-38,-65);
            line(c,0xffb49b91,5,-22,-43,-4,-61);
            line(c,0xffb49b91,5,12,-23,23,-72);line(c,0xffb49b91,5,23,-56,38,-65);
            line(c,0xffb49b91,5,22,-43,4,-61);
        }else if("sprout".equals(shape)){
            line(c,0xff7fa98b,4,0,-29,0,-55);
            oval(c,0xff8fb39a,-20,-61,21,12);oval(c,0xffb3cda7,-1,-68,22,17);
        }else {
            triangle(c,0xffe8d2e9,-27,-16,-32,-54,-5,-30);
            triangle(c,0xffe8d2e9,27,-16,32,-54,5,-30);
            triangle(c,0xffc4a2cb,-23,-30,-28,-46,-10,-29);
            triangle(c,0xffc4a2cb,23,-30,28,-46,10,-29);
        }
        oval(c,0xffead9f0,-35,-32,70,80);
        oval(c,0xfff8ecf5,-25,-27,50,69);
        oval(c,0xffa98cb5,-18,-6,9,13);oval(c,0xffa98cb5,9,-6,9,13);
        oval(c,0x77d9a6b9,-23,13,14,9);oval(c,0x77d9a6b9,9,13,14,9);
        line(c,0xffab90b5,2,-4,17,4,17);
        oval(c,0xfff9e9e1,-32,30,21,14);oval(c,0xfff9e9e1,12,30,21,14);
        c.restore();
    }
    private void atmospheric(Canvas c,String wx){
        if("rain".equals(wx)||"storm".equals(wx)){
            if("storm".equals(wx))fill(c,0x32535b76,0,0,400,900);
            for(int i=0;i<80;i++){
                float x=(i*67+time*63)%430-15, y=(i*121+time*330)%980-30;
                line(c,0x99d5e6ef,1.4f,x,y,x-5,y+16);
            }
        }else if("snow".equals(wx)){
            for(int i=0;i<64;i++){
                float x=(i*57+(float)Math.sin(time+i)*14)%405;
                float y=(i*127+time*48)%930;
                circle(c,0xcef9faff,x,y,(i%3)+1.6f);
            }
        }else if("fog".equals(wx)){
            for(int i=0;i<7;i++)oval(c,0x30f8f6f3,-80,330+i*73,560,75);
        }
    }
}
