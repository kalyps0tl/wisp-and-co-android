package com.wispandco.wallpaper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Locale;

/**
 * A lightweight, privacy-preserving phone home-screen widget.
 * Uses only saved local Wisp state. No background sensor or location service.
 */
public final class WispWidget extends AppWidgetProvider {
    private static PendingIntent open(Context context,Class<?> target,int request,String action){
        Intent intent=new Intent(context,target);
        intent.setAction(action);
        if("com.wispandco.OPEN_TALK".equals(action))intent.putExtra("wisp_open_talk",true);
        return PendingIntent.getActivity(context,request,intent,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void refresh(Context context){
        AppWidgetManager manager=AppWidgetManager.getInstance(context);
        ComponentName provider=new ComponentName(context,WispWidget.class);
        int[] ids=manager.getAppWidgetIds(provider);
        if(ids!=null && ids.length>0)updateWidgets(context,manager,ids);
    }
    private static void updateWidgets(Context context,AppWidgetManager manager,int[] ids){
        WispState.advance(context);
        String name=WispState.name(context);
        String task=WispState.activity(context);
        String mood=ContextEngine.dayPart();
        String zone=SceneRoutes.title(WispState.zone(context));
        String status=ContextEngine.batterySaver(context)?"Battery saver · ": "";
        status+=mood.substring(0,1).toUpperCase(Locale.ROOT)+mood.substring(1)+" · "+zone;
        String speech=WispMoments.pending(context)
            ?"I have a little question for you ♥":
            task.length()>92?task.substring(0,89)+"…":task;
        for(int id:ids){
            RemoteViews view=new RemoteViews(context.getPackageName(),R.layout.widget_wisp);
            view.setTextViewText(R.id.wisp_widget_name,name+"  ✦");
            view.setTextViewText(R.id.wisp_widget_activity,speech);
            view.setTextViewText(R.id.wisp_widget_status,status);
            view.setOnClickPendingIntent(R.id.wisp_widget_root,
                open(context,MainActivity.class,100,"com.wispandco.OPEN_HOME"));
            view.setOnClickPendingIntent(R.id.wisp_widget_explore,
                open(context,ExplorerActivity.class,101,"com.wispandco.OPEN_EXPLORE"));
            view.setOnClickPendingIntent(R.id.wisp_widget_talk,
                open(context,MainActivity.class,102,"com.wispandco.OPEN_TALK"));
            manager.updateAppWidget(id,view);
        }
    }
    @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids){
        updateWidgets(c,manager,ids);
    }
    @Override public void onEnabled(Context c){super.onEnabled(c);refresh(c);}
    @Override public void onReceive(Context c,Intent i){
        super.onReceive(c,i);
        if("com.wispandco.REFRESH_WIDGET".equals(i.getAction()))refresh(c);
    }
}
