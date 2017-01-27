package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;

import timber.log.Timber;

/**
 * Created by rashi on 28/12/16.
 */

public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("in on update no of widgets : "+ appWidgetIds.length);
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Timber.d("in on updatind the widget ");
        CharSequence widgetText = context.getString(R.string.app_name);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
        views.setTextViewText(R.id.app_widget_text, widgetText);
        views.setRemoteAdapter(R.id.stock_list,new Intent(context,StockWidgetListService.class));
        Intent launchIntent=new Intent(context, MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(context,0,launchIntent,0);
        views.setOnClickPendingIntent(R.id.widget,pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
            super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
//        context.startService(new Intent(context, StockWidgetService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("in on receive");

        super.onReceive(context, intent);
        if(QuoteSyncJob.ACTION_WIDGET_UPDATED.equals(intent.getAction())){
            Timber.d("on receive in stock widget provider");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(new ComponentName(context, StockWidgetProvider.class)), R.id.stock_list);
        }
    }
}
