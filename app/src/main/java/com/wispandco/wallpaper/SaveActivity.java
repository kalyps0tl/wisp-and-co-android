package com.wispandco.wallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import org.json.JSONObject;

/** Self-service offline backup, restore and privacy controls. */
public final class SaveActivity extends Activity {
    private static final int REQUEST_EXPORT=102;
    private static final int REQUEST_IMPORT=103;
    private LinearLayout body;
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    private void label(String s,int size){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);
        t.setTextColor(0xff50475f);t.setPadding(0,dp(7),0,dp(12));body.addView(t);
    }
    private void button(String name,Runnable action){
        Button button=new Button(this);button.setText(name);button.setAllCaps(false);
        button.setTextColor(0xff554762);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0xffecdfe9,0xffdbe5e0});bg.setCornerRadius(dp(16));
        button.setBackground(bg);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(55));
        lp.bottomMargin=dp(12);body.addView(button,lp);
        button.setOnClickListener(v->action.run());
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);draw();}
    private void draw(){
        ScrollView sc=new ScrollView(this);sc.setBackgroundColor(0xfffbf6fb);
        body=new LinearLayout(this);body.setPadding(dp(24),dp(26),dp(24),dp(44));
        body.setOrientation(LinearLayout.VERTICAL);sc.addView(body);setContentView(sc);
        label("✦ Your little world is yours",24);
        label("Wisp's personality, name, relationships, adventures and projects live privately on your phone. You can make a local backup, restore one or start fresh whenever you choose.",16);
        button("Export a Wisp save file",this::export);
        button("Restore a Wisp save file",this::importFile);
        button("Erase Wisp's local data",this::confirmErase);
        label("Save files are ordinary JSON documents. You choose where they go using Android's Files picker. Wisp does not upload them, and no online account is required.",14);
        label("Treat exported files as private: they may contain your Wisp's custom name, choices, notes and journal. Removing the app can remove local data, so export before uninstalling or changing phones.",14);
        label("Step sensing and live weather are optional and can be switched off in Context & senses or the main app. Wisp does not record GPS travel routes, read messages or use the microphone.",14);
    }
    private void export(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/json");
        i.putExtra(Intent.EXTRA_TITLE,"WispAndCo-save.json");
        startActivityForResult(i,REQUEST_EXPORT);
    }
    private void importFile(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i,REQUEST_IMPORT);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        if(request==REQUEST_EXPORT){
            try {
                int count=WispBackup.exportTo(this,uri);
                showMessage("Saved","Your Wisp was exported to the location you chose ("+count+" saved details).");
            }catch(Exception e){showMessage("Couldn't save",e.getMessage());}
        }else if(request==REQUEST_IMPORT){
            try{
                JSONObject validated=WispBackup.readValidated(this,uri);
                int count=validated.length();
                String preview=WispState.name(this);
                JSONObject entry=validated.optJSONObject("name");
                if(entry!=null)preview=entry.optString("value",preview);
                final String incoming=preview;
                new AlertDialog.Builder(this).setTitle("Restore Wisp's world?")
                    .setMessage("This save contains "+count+" details and a companion named "+
                        incoming+". It will replace matching local settings and memories. Export your current world first if you want to keep both.")
                    .setPositiveButton("Restore",(d,which)->{
                        try{
                            int restored=WispBackup.importFrom(this,validated);
                            showMessage("World restored",restored+" details restored. Reopen Wisp's main screen to see the changes.");
                        }catch(Exception e){showMessage("Couldn't restore",e.getMessage());}
                    })
                    .setNegativeButton("Cancel",null).show();
            }catch(Exception e){showMessage("Not a compatible save",e.getMessage());}
        }
    }
    private void confirmErase(){
        new AlertDialog.Builder(this).setTitle("Erase all of Wisp's memories?")
            .setMessage("This will permanently remove the companion's saved identity, journal, discoveries, seasonal activities, context choices and growth from this device. It cannot be undone without a previously exported save.")
            .setNegativeButton("Keep my Wisp",null)
            .setPositiveButton("Continue",(d,i)->
                new AlertDialog.Builder(this).setTitle("Final confirmation")
                .setMessage("Have you exported a backup if you want one? This deletes all local Wisp data.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Erase local world",(dialog,which)->{
                    WispBackup.erase(this);
                    showMessage("Wisp's world cleared","You can meet a new Wisp the next time you open the app.");
                }).show()).show();
    }
    private void showMessage(String title,String body){
        new AlertDialog.Builder(this).setTitle(title)
            .setMessage(body==null?"An unexpected error occurred.":body)
            .setPositiveButton("Okay",null).show();
    }
}
