package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import timber.log.Timber;

/**
 * Created by abc on 1/4/17.
 */

public class StockWidgetService extends IntentService {

    private static final String[] QUOTE_COLUMNS = {
        Contract.Quote._ID,
        Contract.Quote.COLUMN_SYMBOL,
        Contract.Quote.COLUMN_PRICE,
        Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
        Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
        Contract.Quote.COLUMN_HISTORY
    };

    public static final int POSITION_ID = 0;
    public static final int POSITION_SYMBOL = 1;
    public static final int POSITION_PRICE = 2;
    public static final int POSITION_ABSOLUTE_CHANGE = 3;
    public static final int POSITION_PERCENTAGE_CHANGE = 4;
    public static final int POSITION_HISTORY = 5;

    public StockWidgetService() {
        super(StockWidgetService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                StockWidgetProvider.class));

        Uri quoteUri = Contract.Quote.uri;
        Timber.d(quoteUri.toString());

        Cursor data = getContentResolver().query(quoteUri, QUOTE_COLUMNS, null, null, null);

        if(data == null){
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        String symbol = data.getString(POSITION_SYMBOL);

        Timber.d(symbol + " is the symbol" + "app widget id length : "+ appWidgetIds.length);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.stock_widget);
            views.setTextViewText(R.id.app_widget_text, symbol);
            Intent launchIntent=new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,launchIntent,0);
            views.setOnClickPendingIntent(R.id.widget,pendingIntent);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
