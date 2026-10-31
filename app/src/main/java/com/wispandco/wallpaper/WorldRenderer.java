package com.wispandco.wallpaper;

import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import java.util.Calendar;

/** Independent native scene renderer. All drawing uses a fixed 400x900 design canvas. */
public final class WorldRenderer {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r=new RectF();
    private float time;
    private String weather;
    // Scene geometry is expensive at full phone resolution. Cache the still
    // 600x1350 storybook scenery and animate only Wisp, wildlife and weather.
    private Bitmap sceneCache;
    private float walkingX = Float.NaN;
    private float destinationX = Float.NaN;
    private String walkingZone = "";
    public void setDestination(float x){
        destinationX=Math.max(52,Math.min(346,x));
    }
    private String cacheZone = "";
    private String cacheWeather = "";
    private String cacheSeason = "";
    private boolean cacheNight;
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

    public void release() {
        if (sceneCache != null) {
            sceneCache.recycle();
            sceneCache = null;
        }
    }
    private void cacheScene(String zone, String wx, boolean night) {
        String season=season();
        if (sceneCache != null && !sceneCache.isRecycled()
                && cacheZone.equals(zone) && cacheWeather.equals(wx) && cacheNight == night && cacheSeason.equals(season)) return;
        if (sceneCache == null || sceneCache.isRecycled()) {
            sceneCache = Bitmap.createBitmap(600, 1350, Bitmap.Config.ARGB_8888);
        }
        Canvas backing = new Canvas(sceneCache);
        backing.save();
        backing.scale(1.5f, 1.5f);
        this.time = 0f; // Background stays still; animation is a separate layer.
        this.weather = wx;
        sky(backing, night);
        if ("cottage".equals(zone)) {
            interior(backing);
        } else {
            mountains(backing);
            if ("forest".equals(zone)) forest(backing);
            else if ("garden".equals(zone)) garden(backing);
            else if ("pond".equals(zone)) pond(backing);
            else dock(backing);
        }
        scenicDetails(backing,zone);
        seasonalDetails(backing,zone,season);
        backing.restore();
        cacheZone = zone;
        cacheWeather = wx;
        cacheNight = night;
        cacheSeason=season;
    }
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
    public void draw(Canvas c,int w,int h,String zone,String weather,String spirit,long timestamp,
                     int stage,String trait,int pumpkins,int tree,int candles){
        if(w<=0||h<=0)return;
        boolean night=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=19
                || Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<6;
        cacheScene(zone, weather, night);
        c.save();
        // Preserve proportions on differently shaped Android displays. Slight
        // cropping is preferable to squashing Wisp and its cottage.
        float scale=Math.max(w/400f,h/900f);
        c.translate((w-400f*scale)/2f,(h-900f*scale)/2f);
        c.scale(scale,scale);
        c.drawBitmap(sceneCache, null, new RectF(0, 0, 400, 900), bitmapPaint);
        time=(timestamp%1000000L)/1000f;
        this.weather=weather;
        if ("pond".equals(zone) || "dock".equals(zone)) movingWater(c);
        wildlife(c,zone);
        if("cottage".equals(zone))decorations(c,pumpkins,tree,candles);
        wisp(c,spirit,zone,stage,trait);
        foreground(c,zone);
        atmospheric(c,weather);
        c.restore();
    }
    private static String season(){
        int month=Calendar.getInstance().get(Calendar.MONTH);
        if(month==11||month<=1)return "winter";
        if(month<=4)return "spring";
        if(month<=7)return "summer";
        return "autumn";
    }
    private void seasonalDetails(Canvas c,String zone,String season){
        if("cottage".equals(zone)){
            if("winter".equals(season)){
                for(int i=0;i<7;i++)circle(c,0x99eff4f5,198+(i*17)%85,231+(i*31)%92,2.5f);
            }else if("autumn".equals(season)){
                oval(c,0xffbb8d7b,215,317,25,6);
                oval(c,0xffd3a881,249,324,15,5);
            }
            return;
        }
        if("winter".equals(season)){
            // Snowflakes along the ground remain separate from active snowfall.
            for(int i=0;i<21;i++){
                float x=(i*71)%425-15;
                float y=600+(i%5)*53;
                oval(c,0xbbf7f4ef,x,y,28+i%3*8,9);
            }
        }else if("autumn".equals(season)){
            int[] leaves={0xffd8a27b,0xffbd8f85,0xffe1bb89,0xffab857c};
            for(int i=0;i<31;i++){
                float x=(i*93)%408,y=590+(i*37)%270;
                oval(c,leaves[i%4],x,y,8+i%3*3,4+i%2*3);
            }
        }else if("spring".equals(season)){
            for(int i=0;i<28;i++){
                float x=(i*79)%410,y=593+(i*53)%240;
                circle(c,0xfff1d7e1,x,y,4);
                circle(c,0xffffefd2,x+1,y,1.5f);
            }
        }else{
            for(int i=0;i<24;i++){
                float x=(i*67)%408,y=628+(i*43)%240;
                line(c,0xaa739b81,2,x,y,x-2,y-9);
            }
        }
    }
    /** Low-cost details baked into the cached background, not every frame. */
    private void scenicDetails(Canvas c,String zone){
        if("cottage".equals(zone)){
            // Worn woven rug, framed little painting, plant, books and a lamp.
            oval(c,0xff987e8d,80,751,252,90);
            oval(c,0xffd6b5bb,95,760,221,71);
            oval(c,0xffefe1d5,120,772,175,44);
            for(int i=0;i<7;i++)line(c,0xffc39ba9,2,132+i*22,775,143+i*22,815);
            round(c,0xff9e8b8e,17,330,52,75,4);
            round(c,0xffe8ddce,22,335,42,65,2);
            triangle(c,0xffa0baa5,28,378,43,347,60,378);
            circle(c,0xfff7d9a6,49,349,6);
            round(c,0xff877b7c,337,625,44,11,3);
            round(c,0xffad928b,352,629,7,67,2);
            round(c,0xffae9589,341,604,31,25,5);
            for(int i=0;i<5;i++) {
                float a=i*6.283185f/5f;
                line(c,0xff739c7f,3,355,605,355+(float)Math.sin(a)*23,592+(float)Math.cos(a)*18);
                oval(c,0xff90b49a,346+(float)Math.sin(a)*23,580+(float)Math.cos(a)*18,17,10);
            }
            round(c,0xffebd7ab,37,614,64,19,3);
            for(int i=0;i<5;i++)round(c,0xff8e7892,41+i*12,603-i%2*3,8,15,2);
            return;
        }
        // Reeds, flower patches, stones and moss give the landscape depth.
        for(int i=0;i<8;i++){
            float x=11+i*55;
            float y=674+(i%4)*51;
            oval(c,0x6680a39a,x,y,47,23);
            if("pond".equals(zone)||"dock".equals(zone)){
                line(c,0xff6b9886,2,x+18,y+28,x+14,y-17);
                line(c,0xff779f8e,2,x+22,y+30,x+30,y-12);
                oval(c,0xffc9b4a6,x+10,y-19,7,15);
                oval(c,0xffc9b4a6,x+27,y-16,6,13);
            }else {
                for(int j=0;j<3;j++){
                    float px=x+5+j*12,py=y+6+j%2*10;
                    circle(c,i%2==0?0xffd7bdd4:0xfff3e2ae,px,py,4);
                    circle(c,0xfff8edca,px,py,1.5f);
                }
            }
        }
        if("forest".equals(zone)){
            // Soft moss and tiny shelf mushrooms on foreground trunks.
            for(int i=0;i<4;i++){
                float x=i*99+16,y=487+(i%2)*80;
                oval(c,0xffac91a0,x,y,24,9);
                oval(c,0xffead6c7,x+3,y+7,19,6);
            }
        }
    }
    private void foreground(Canvas c,String zone){
        if("cottage".equals(zone))return;
        // Sparse foreground framing creates parallax-like depth rather than
        // leaving the character pasted onto a flat single-plane image.
        for(int side=0;side<2;side++){
            float origin=side==0?0:400;
            for(int i=0;i<6;i++){
                float x=origin+(side==0?1:-1)*(i*12+6);
                float h=40+i%3*11;
                line(c,0x99718d83,2,x,925,x+(side==0?12:-12),872-h);
                if(i%2==0)oval(c,0xaa9bb5a1,
                    Math.min(x,x+(side==0?20:-20))-8,858-h,
                    31,14);
            }
        }
    }
    private void movingWater(Canvas c) {
        // A few ripples give the pond movement without redrawing the scenery.
        for (int i=0; i<5; i++) {
            float x = 48+i*71+(float)Math.sin(time*0.7+i)*7;
            float y = 789+(i%3)*22;
            line(c, 0x66e8f5f0, 1.2f, x, y, x+23, y);
        }
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
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean night=hour>=19||hour<6;
        if("forest".equals(zone)){
            // Alder peers from a branch. Owl eyes open after dusk.
            oval(c,0xff8d7789,285,430,46,63);
            triangle(c,0xff8d7789,289,444,292,420,303,443);
            triangle(c,0xff8d7789,312,443,324,420,328,445);
            oval(c,night?0xffe7d9a8:0xffc0a7a8,295,444,13,12);
            oval(c,night?0xffe7d9a8:0xffc0a7a8,313,444,13,12);
            if(night){circle(c,0xff51445d,301,450,3);circle(c,0xff51445d,319,450,3);}
            triangle(c,0xffd9b18c,306,461,314,461,310,469);
            line(c,0xff877382,3,266,489,354,489);
        }
        if("cottage".equals(zone)){
            // Fenn, the moth who lives beside the bookshelf.
            float mothY=371+(float)Math.sin(time*1.3)*8;
            oval(c,0x99ddc7e5,145,mothY-12,18,26);
            oval(c,0x99f5dfea,162,mothY-12,18,26);
            oval(c,0xffb998b6,160,mothY-7,7,22);

            for(int i=0;i<5;i++){
                float x=306+i*9,y=310+(float)Math.sin(time*1.2+i)*6;
                circle(c,0x99ffe9b0,x,y,2.1f);
            }
            return;
        }
        if(!"storm".equals(weather)&&!"snow".equals(weather) && !night){
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
    private void decorations(Canvas c,int pumpkin,int tree,int candles){
        if(pumpkin>0) {
            // Hand-carved pumpkin on Wisp's wooden workbench.
            oval(c,0xffc18e5e,38,640,43,33);
            oval(c,0xffe5a76c,42,629,38,37);
            line(c,0xff789078,4,61,633,63,622);
            if(pumpkin>=2)triangle(c,0xff665364,50,644,54,637,58,644);
            if(pumpkin>=3)triangle(c,0xff665364,65,644,69,637,73,644);
            if(pumpkin>=4) {
                line(c,0xff665364,2,51,651,72,651);
                circle(c,0xffffedba,62,649,3);
            }
        }
        if(tree>0){
            line(c,0xff876f6a,6,191,712,191,510);
            triangle(c,0xff6f9a88,137,660,191,514,245,660);
            triangle(c,0xff82ad93,145,603,191,483,236,603);
            triangle(c,0xff9cbd9c,153,553,191,466,229,553);
            if(tree>=2)circle(c,0xfffbe4bb,191,476,7);
            int count=Math.max(0,tree-2)*2;
            for(int i=0;i<count;i++){
                int color=i%3==0?0xffffcfaa:i%3==1?0xffe3c7ea:0xfff6e5bd;
                circle(c,color,172+(i*29)%42,556+(i*19)%94,4);
            }
        }
        if(candles>0){
            // Nine-branched decorative hanukkiah; central shamash is higher.
            // A personal religious observance is not simulated automatically.
            line(c,0xffbfa479,4,260,630,360,630);
            line(c,0xffbfa479,5,309,630,309,654);
            for(int i=0;i<9;i++){
                float x=264+i*12;
                float top=i==4?591:603;
                line(c,0xffbfa479,3,x,631,x,top+10);
                round(c,0xfff4e8cd,x-3,top,6,14,2);
                if(i==4 || i<4&&i<candles || i>4&&i-1<candles) {
                    oval(c,0xffffc975,x-3,top-9,6,10);
                }
            }
        }
    }
    private void wisp(Canvas c,String shape,String zone,int stage,String trait){
        float y="cottage".equals(zone)?738:("pond".equals(zone)?695:("garden".equals(zone)?724:("forest".equals(zone)?716:705)));
        float baseX="dock".equals(zone)?294:("garden".equals(zone)?177:("pond".equals(zone)?161:211));
        if(!zone.equals(walkingZone)){
            walkingZone=zone;walkingX=baseX;destinationX=Float.NaN;
        }
        float goal=Float.isNaN(destinationX)?baseX+(float)Math.sin(time*.30f)*34:destinationX;
        if(Float.isNaN(walkingX))walkingX=baseX;
        walkingX+=(goal-walkingX)*.12f;
        float step=(float)Math.sin(time*5f)*Math.min(3,Math.abs(goal-walkingX)*.08f);
        float bob=(float)Math.sin(time*2.2)*2;
        c.save();c.translate(walkingX,y+bob+step);
        float growth=1f+stage*.085f;
        c.scale(growth,growth);
        oval(c,0x447b6c89,-31,37,65,12);
        // Visible feet and tail make Wisp a little embodied creature, not a floating sphere.
        oval(c,0xffc9b3d9,-28,25,23,21);
        oval(c,0xffc9b3d9,7,25,23,21);
        oval(c,0xffdbcae4,-41,1,28,25);
        if("moth".equals(shape)){
            oval(c,0x99d9c9e7,-51,-28,39,54);
            oval(c,0x99e8d7ee,12,-28,39,54);
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
        oval(c,0xffead9f0,-35,-32,70,75);
        oval(c,0xfff8ecf5,-25,-26,50,60);
        // Short reaching arms and expressive eyes.
        oval(c,0xffdbc8e8,-41,-1,19,31);
        oval(c,0xffdbc8e8,22,0,19,31);
        oval(c,0xffa98cb5,-18,-8,9,13);
        oval(c,0xffa98cb5,9,-8,9,13);
        circle(c,0xfffdf9ff,-15,-5,2);
        circle(c,0xfffdf9ff,12,-5,2);
        oval(c,0x77d9a6b9,-23,12,13,8);
        oval(c,0x77d9a6b9,10,12,13,8);
        line(c,0xffab90b5,2,-4,14,4,14);
        if(stage>0){
            int marking="moss".equals(trait)?0xff7cab8b:
                "petals".equals(trait)?0xffe7b4cc:
                "frost".equals(trait)?0xffa3dce4:
                "embers".equals(trait)?0xfff2bb8c:0xffdfd6a1;
            circle(c,marking,0,-16,4);
            if(stage>1){
                circle(c,marking,-16,8,3.4f);
                circle(c,marking,16,8,3.4f);
            }
            if(stage>2){
                triangle(c,marking,-7,-29,0,-43,7,-29);
                circle(c,marking,0,-45,3);
            }
            if(stage>3) {
                for(int i=0;i<5;i++){
                    double a=time*.25+i*6.283/5;
                    circle(c,0x99ffefcd,(float)Math.cos(a)*47,(float)Math.sin(a)*38,2.5f);
                }
            }
        }
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
