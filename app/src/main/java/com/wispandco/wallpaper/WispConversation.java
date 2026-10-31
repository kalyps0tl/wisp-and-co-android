package com.wispandco.wallpaper;

import android.app.Activity;
import android.app.AlertDialog;

/** One shared invitation dialog usable from home and world exploration. */
public final class WispConversation {
    private WispConversation(){}
    public static void show(Activity activity,Runnable onChange){
        WispMoments.invite(activity);
        String question=WispMoments.question(activity);
        String[] answers=WispMoments.answers(activity);
        if(answers.length!=3)return;
        new AlertDialog.Builder(activity).setTitle("A little question from "+WispState.name(activity))
            .setMessage(question)
            .setItems(answers,(dialog,which)->{
                String response=WispMoments.reply(activity,which);
                new AlertDialog.Builder(activity).setTitle("Wisp remembers...")
                    .setMessage(response)
                    .setPositiveButton("♥",null).show();
                if(onChange!=null)onChange.run();
            })
            .setNeutralButton("Ask me later",(dialog,which)->{
                WispMoments.later(activity);
                if(onChange!=null)onChange.run();
            })
            .setNegativeButton("Not this time",(dialog,which)->{
                WispMoments.dismiss(activity);
                if(onChange!=null)onChange.run();
            }).show();
    }
}
