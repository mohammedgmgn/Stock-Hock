package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

/**
 * Created by abc on 1/4/17.
 */

public class StockWidgetListService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            private final String[] QUOTE_COLUMNS = {
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
            public void onCreate() {
                data = getContentResolver().query(Contract.Quote.uri, QUOTE_COLUMNS, null, null, null);
            }

            @Override
            public void onDataSetChanged() {
                if(data != null){
                    data.close();
                }
                data = getContentResolver().query(Contract.Quote.uri, QUOTE_COLUMNS, null, null, null);
            }

            @Override
            public void onDestroy() {
                if(data!=null){
                    data.close();
                }
            }

            @Override
            public int getCount() {
                if(data!=null){
                    return data.getCount();
                }
                return 0;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(position == AdapterView.INVALID_POSITION || data == null){
                    return null;
                }
                data.moveToPosition(position);
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                String symbol = data.getString(POSITION_SYMBOL);
                Double price = 0.1*data.getDouble(POSITION_PRICE);
                Double abs_change = 0.1*data.getDouble(POSITION_ABSOLUTE_CHANGE);
                Double per_change = 0.1*data.getDouble(POSITION_PERCENTAGE_CHANGE);
                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, "$" + String.format("%.2f", price));
                if(per_change < 0 ){
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                    views.setTextViewText(R.id.change, String.format("%.2f", per_change)+"%");
                }
                else {
                    views.setTextViewText(R.id.change, "+"+ String.format("%.2f", per_change)+"%");
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
                data.moveToPosition(position);
                return data.getInt(POSITION_ID);
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
