package com.wispandco.wallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * First-meeting sequence. Answers suggest a companion; the user always gets
 * the final say. Existing Wisp state is changed only after confirmation.
 */
public final class MatchingActivity extends Activity {
    private static final String[] NAMES={
        "Wayfinder", "Tinkerer", "Hearthkeeper", "Dreamweaver"
    };
    private static final String[] PROFILES={
        "wonder", "maker", "hearth", "dreamer"
    };
    private static final String[] FORMS={
        "antlered", "sprout", "woodland", "moth"
    };
    private static final String[] DESCRIPTIONS={
        "A curious explorer who follows half-forgotten paths, befriends creatures and returns with curious finds.",
        "A resourceful little inventor, forever building, fixing, planting and surprising itself with new ideas.",
        "A gentle keeper of cozy places, patient friendships, garden beds and the quiet beauty of ordinary days.",
        "An imaginative night-wanderer drawn to fireflies, strange stories, moonlit water and small, secret wonders."
    };
    private static final String[] QUESTIONS={
        "You find a tiny unmarked door beneath the roots of a tree. What draws you closer?",
        "A whole afternoon opens up without any plans. Which possibility sounds loveliest?",
        "Something mysterious has washed up by the pond. What's your first instinct?",
        "Wisp is feeling uncertain about a new friend. How might you help?",
        "A storm rolls over the hills. What's the perfect way to spend it?",
        "The world offers one small gift. Which would you treasure most?"
    };
    private static final String[][] ANSWERS={
        {"Discover where the door leads","Imagine what I could make for it","Make sure its owner is safe","Wonder what stories it has heard"},
        {"Follow a trail I've never taken","Build a tiny handmade invention","Tend a garden and make tea","Watch the sky and invent stories"},
        {"Investigate the unfamiliar shoreline","See how it works and repurpose it","Find a safe place to keep it","Sketch it and imagine its origins"},
        {"Suggest a small shared adventure","Make something to break the ice","Sit quietly and offer reassurance","Write them a gentle little note"},
        {"Look for a rainbow afterward","Work on a project by lamplight","Make the cottage especially cozy","Listen to thunder and dream"},
        {"A map of secret paths","A box of curious materials","A handmade keepsake","A bottle of moonlight"}
    };
    // Three of each response reflect the corresponding disposition across
    // six situations, with no personal data or sensitive traits inferred.
    private final int[] counts={0,0,0,0};
    private final int[] picked={-1,-1,-1,-1,-1,-1};
    private int step=-1;
    private int match=0;
    private String selectedForm="";
    private LinearLayout body;
    private ScrollView scroll;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(state!=null){
            step=state.getInt("quiz_step",-1);
            int[] previous=state.getIntArray("quiz_picks");
            if(previous!=null && previous.length==picked.length){
                System.arraycopy(previous,0,picked,0,picked.length);
                recalculate();
            }
            selectedForm=state.getString("quiz_form",selectedForm);
            FORM_DESIGN_CHOSEN=state.getBoolean("quiz_custom_form",false);
        }
        drawScreen();
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putInt("quiz_step",step);
        out.putIntArray("quiz_picks",picked.clone());
        out.putString("quiz_form",selectedForm);
        out.putBoolean("quiz_custom_form",FORM_DESIGN_CHOSEN);
        super.onSaveInstanceState(out);
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable background(int a,int b,int radius){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});
        d.setCornerRadius(dp(radius));return d;
    }
    private TextView text(String s,int size,boolean strong){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);
        t.setTextColor(0xff4c4663);
        if(strong)t.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
        t.setPadding(dp(5),dp(7),dp(5),dp(8));body.addView(t);return t;
    }
    private void button(String title,boolean primary,Runnable action){
        Button b=new Button(this);b.setAllCaps(false);b.setText(title);b.setTextSize(15);
        b.setTextColor(0xff4e425a);
        b.setBackground(background(primary?0xfff4d5e6:0xffe7e9e6,
            primary?0xffc9d7eb:0xffe3d8e9,18));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(60));
        lp.bottomMargin=dp(11);
        body.addView(b,lp);b.setOnClickListener(v->action.run());
    }
    private void drawScreen(){
        scroll=new ScrollView(this);scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xfffaf6fb);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(21),dp(22),dp(21),dp(44));
        scroll.addView(body);setContentView(scroll);
        text("✦  W I S P  &  C O .",16,true);
        if(step<0) introduction();
        else if(step>=QUESTIONS.length) result();
        else question();
        scroll.post(()->scroll.scrollTo(0,0));
    }
    private void portrait(int spirit){
        SpiritPortrait p=new SpiritPortrait(spirit);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(235));
        lp.bottomMargin=dp(10);
        body.addView(p,lp);
    }
    private void introduction(){
        portrait(2);
        text("A little spirit is looking for you.",24,true);
        text("Somewhere beyond the cottage, four very different spirits are deciding whether to say hello. There are no right or wrong answers—just a few moments to discover which one might feel like home.",16,false);
        text("Six storybook questions · about two minutes · no permissions required",13,false);
        button("Begin our first meeting  ✦",true,()->{step=0;drawScreen();});
        button("Explore first — meet later",false,this::dismiss);
        if(WispState.prefs(this).getBoolean("matching_complete",false)){
            text("Your existing companion will stay exactly as it is unless you confirm a new match.",13,false);
        }
    }
    private void question(){
        int preview=picked[Math.max(0,step-1)];
        portrait(preview>=0?formIndex(FORMS[preview]):2);
        text("A SMALL CONVERSATION   "+(step+1)+" OF "+QUESTIONS.length,13,true);
        text(QUESTIONS[step],23,true);
        for(int i=0;i<4;i++){
            final int answer=i;
            button(ANSWERS[step][i],true,()->{
                picked[step]=answer;
                recalculate();
                step++;
                drawScreen();
            });
        }
        if(step>0)button("← Previous question",false,()->{step--;drawScreen();});
        button("Not right now",false,this::dismiss);
    }
    private void recalculate(){
        for(int i=0;i<counts.length;i++)counts[i]=0;
        for(int answer:picked)if(answer>=0&&answer<counts.length)counts[answer]+=2;
        int best=0;
        for(int i=1;i<4;i++)
            if(counts[i]>counts[best] || (counts[i]==counts[best] && i==picked[5]))
                best=i;
        match=best;
        if(selectedForm.isEmpty())selectedForm=FORMS[match];
    }
    private void result(){
        recalculate();
        // If this is a first visit to results, start with the matching silhouette.
        if(!FORM_DESIGN_CHOSEN)selectedForm=FORMS[match];
        portrait(formIndex(selectedForm));
        text("A spirit has stepped into the clearing...",18,false);
        text("Meet the "+NAMES[match],27,true);
        text(DESCRIPTIONS[match],16,false);
        text("Suggested form: "+formLabel(selectedForm)+". Personality: "+NAMES[match]+". This is an invitation, not a diagnosis or a fixed identity.",14,false);
        button("Choose this spirit's appearance",false,()->new AlertDialog.Builder(this)
            .setTitle("How does your Wisp look?")
            .setItems(new String[]{"Woodland ears","Little antlers","Moth wings","Leaf sprout"},(d,i)->{
                selectedForm=WispState.SPIRITS[i];
                FORM_DESIGN_CHOSEN=true;
                drawScreen();
            }).show());
        button("Meet this Wisp  ♥",true,()->confirm(selectedForm));
        button("Keep my current spirit form",false,()->confirm(WispState.spirit(this)));
        button("Retake the meeting",false,()->{
            for(int i=0;i<picked.length;i++)picked[i]=-1;
            selectedForm="";FORM_DESIGN_CHOSEN=false;step=0;drawScreen();
        });
    }
    private boolean FORM_DESIGN_CHOSEN=false;
    private String formLabel(String form){
        if("antlered".equals(form))return "antlered";
        if("moth".equals(form))return "moth-winged";
        if("sprout".equals(form))return "leaf-sprouted";
        return "woodland";
    }
    private int formIndex(String form){
        if("antlered".equals(form))return 1;
        if("moth".equals(form))return 2;
        if("sprout".equals(form))return 3;
        return 0;
    }
    private void confirm(String form){
        EditText name=new EditText(this);
        name.setSingleLine(true);
        name.setText(WispState.name(this));
        name.setSelectAllOnFocus(true);
        LinearLayout holder=new LinearLayout(this);
        holder.setPadding(dp(22),0,dp(22),0);
        holder.addView(name,new LinearLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("And what shall we call this spirit?")
            .setMessage("You can always change this later.")
            .setView(holder)
            .setPositiveButton("Welcome home",(dialog,which)->{
                String n=name.getText().toString().trim();
                if(n.isEmpty())n="Wisp";
                SharedPreferences.Editor edit=WispState.prefs(this).edit();
                edit.putString("name",n).putString("profile",PROFILES[match])
                    .putString("spirit",form).putString("matched_profile",PROFILES[match])
                    .putBoolean("matching_complete",true)
                    .putBoolean("matching_dismissed",false)
                    .putString("note","I think we're going to make wonderful little discoveries together.")
                    .apply();
                WorldJournal.add(this,"Met "+n+", a "+NAMES[match]+" with "+form+" features.");
                finish();
            }).setNegativeButton("Back",null).show();
    }
    private void dismiss(){
        WispState.prefs(this).edit().putBoolean("matching_dismissed",true).apply();
        finish();
    }
    @Override public void onBackPressed(){
        if(step>0 && step<QUESTIONS.length) {
            step--;drawScreen();
        }else dismiss();
    }
    private final class SpiritPortrait extends View {
        final int silhouette;
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        SpiritPortrait(int spirit){super(MatchingActivity.this);silhouette=spirit;}
        void oval(Canvas c,int color,float l,float t,float r,float b){
            paint.setShader(null);paint.setColor(color);c.drawOval(l,t,r,b,paint);
        }
        void line(Canvas c,int color,float sw,float x,float y,float xx,float yy){
            paint.setColor(color);paint.setShader(null);paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(sw);paint.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(x,y,xx,yy,paint);paint.setStyle(Paint.Style.FILL);
        }
        void polygon(Canvas c,int color,float... pts){
            paint.setColor(color);paint.setShader(null);Path p=new Path();p.moveTo(pts[0],pts[1]);
            for(int i=2;i<pts.length;i+=2)p.lineTo(pts[i],pts[i+1]);p.close();c.drawPath(p,paint);
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            canvas.save();
            canvas.scale(getWidth()/360f,getHeight()/220f);
            paint.setShader(new LinearGradient(0,0,360,220,0xffb7becf,0xfff6ddcf,Shader.TileMode.CLAMP));
            canvas.drawRoundRect(0,0,360,220,22,22,paint);paint.setShader(null);
            oval(canvas,0x44faf2da,105,21,257,175);
            oval(canvas,0x99bfc7c2,-10,179,378,267);
            for(int i=0;i<18;i++){
                float x=(i*79+29)%340,y=(i*47+13)%142;
                oval(canvas,0x99fef5df,x,y,x+2.5f,y+2.5f);
            }
            canvas.save();canvas.translate(180,115);
            if(silhouette==2){
                oval(canvas,0xb2d4c4ec,-69,-31,-9,41);
                oval(canvas,0xb2e1d0ef,9,-31,69,41);
                line(canvas,0xff977aa4,3,-22,-42,-35,-73);
                line(canvas,0xff977aa4,3,22,-42,35,-73);
            }else if(silhouette==1){
                line(canvas,0xffa89089,5,-25,-29,-43,-78);
                line(canvas,0xffa89089,5,25,-29,43,-78);
                line(canvas,0xffa89089,4,-33,-51,-59,-64);
                line(canvas,0xffa89089,4,33,-51,59,-64);
            }else if(silhouette==3){
                line(canvas,0xff7d9d8b,4,0,-58,0,-88);
                oval(canvas,0xff7b9f8d,-39,-98,2,-77);
                oval(canvas,0xffa4c6a0,0,-103,36,-80);
            }else {
                polygon(canvas,0xffe7d8eb,-38,-17,-43,-65,-5,-40);
                polygon(canvas,0xffe7d8eb,38,-17,43,-65,5,-40);
            }
            oval(canvas,0xffe5d7ef,-49,-46,49,61);
            oval(canvas,0xfff6e9f2,-36,-36,36,47);
            oval(canvas,0xffd4bddf,-51,14,-34,48);
            oval(canvas,0xffd4bddf,34,14,51,48);
            oval(canvas,0xff9f81ad,-25,-6,-14,9);
            oval(canvas,0xff9f81ad,14,-6,25,9);
            oval(canvas,0xffefb9ca,-30,17,-12,26);
            oval(canvas,0xffefb9ca,12,17,30,26);
            line(canvas,0xffb38fae,2,-4,20,4,20);
            oval(canvas,0xffe8dcea,-38,47,-6,67);
            oval(canvas,0xffe8dcea,6,47,38,67);
            canvas.restore();canvas.restore();
        }
    }
}
