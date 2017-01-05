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

/*
RemoteViewsService and RemoteViewsFactory function almost exactly like an Adapter.
 It is provided data from some source, usually a ContentProvider, and it creates Views and binds them to a View that can display them.
  RemoteViewsFactory is just a wrapper for an Adapter really, and it provides the methods we need to create views and bind data
  like getCount() and getViewAt()
* */
public class QuoteWidgetRemoteViewsService extends IntentService {
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

    public QuoteWidgetRemoteViewsService(String name) {
        super(name);
    }

    //   @Override
   // public RemoteViewsFactory onGetViewFactory(Intent intent) {
    /*
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                // This is the same query from MyStocksActivity
                data = getContentResolver().query(
                        Contract.Quote.uri,
                        QUOTE_COLUMNS,
                      null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }

            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                // Get the layout
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_stocks);

                // Bind data to the views
                views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex("symbol")));

                if (data.getInt(data.getColumnIndex("is_up")) == 1) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

              //  if (Utils.showPercent) {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("percentage_change")));
             //   }
                /*else {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("absolute_change")));
                }*/

                /*final Intent fillInIntent = new Intent();

    public QuoteWidgetRemoteViewsService() {
        super(QuoteWidgetRemoteViewsService.class.getSimpleName());
    }/*
/*
    fillInIntent.putExtra("symbol", data.getString(data.getColumnIndex("symbol")));
                views.setOnClickFillInIntent(R.id.quote, fillInIntent);

                return views;
            }
*/
          /*  @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                // Get the row ID for the view at the specified position
                if (data != null && data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }*/


    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                QuoteWidgetProvider.class));

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
