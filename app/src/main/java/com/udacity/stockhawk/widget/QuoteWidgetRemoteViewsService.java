package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

/*
RemoteViewsService and RemoteViewsFactory function almost exactly like an Adapter.
 It is provided data from some source, usually a ContentProvider, and it creates Views and binds them to a View that can display them.
  RemoteViewsFactory is just a wrapper for an Adapter really, and it provides the methods we need to create views and bind data
  like getCount() and getViewAt()
* */
public class QuoteWidgetRemoteViewsService extends RemoteViewsService  {
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

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;


            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.uri,Contract.Quote.QUOTE_COLUMNS,
                        Contract.Quote.COLUMN_SYMBOL + " = ?",new String[]{"1"},null);

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

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
                String symbol = data.getString(data.getColumnIndex("symbol"));

                views.setTextViewText(R.id.widget_stock_symbol, "hi siko");
                views.setTextViewText(R.id.widget_price, data.getString(data.getColumnIndex("price")));
                views.setTextViewText(R.id.widget_change, data.getString(data.getColumnIndex("percentage_change")));


                if (data.getInt(data.getColumnIndex("is_up")) == 1) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(data.getColumnIndexOrThrow(Contract.Quote._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

        };
    }

}