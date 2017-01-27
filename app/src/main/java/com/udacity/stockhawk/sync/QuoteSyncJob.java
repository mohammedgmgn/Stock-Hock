package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    public static final String ACTION_WIDGET_UPDATED = "android.appwidget.action.ACTION_WIDGET_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;

    static void getQuotes(final Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -2);

        try {
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);
            Handler handler=new Handler(Looper.getMainLooper());

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }
            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Log.v("test",quotes.toString());
            Iterator<String> iterator = stockCopy.iterator();
            ArrayList<ContentValues> quoteCVs = new ArrayList<>();
            while (iterator.hasNext()) {
                final String symbol = iterator.next();

                try {

                    Stock stock = quotes.get(symbol);
                    StockQuote quote = stock.getQuote();

                    float price = quote.getPrice().floatValue();
                    float change = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();

                    // WARNING! Don't request historical data for a stock that doesn't exist!
                    // The request will hang forever X_x
                    List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);
                    StringBuilder historyBuilder = new StringBuilder();

                    for (HistoricalQuote it : history) {
                        historyBuilder.append(it.getDate().getTimeInMillis());
                        historyBuilder.append(", ");
                        historyBuilder.append(it.getClose());
                        historyBuilder.append("\n");
                    }
                    ContentValues quoteCV = new ContentValues();
                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                    quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                    quoteCVs.add(quoteCV);
                    // If stock is invalid, display invalid ticker toast message by posting message to UI Thread
                context.getContentResolver()
                        .bulkInsert(
                                Contract.Quote.uri,
                                quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
                context.sendBroadcast(dataUpdatedIntent);

            } catch (Exception e)
                {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        PrefUtils.editStockPref(context,symbol,false);
                            displayInvalidToast(context,symbol);
                        }
                    });

                }
            }

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    public static void displayValidToast(Context context,String symbol) {
        Toast.makeText(context, R.string.success_stock_ticker, Toast.LENGTH_SHORT).show();

    }

    public static void displayInvalidToast(Context context,String symbol) {
        Toast.makeText(context, context.getString(R.string.invalid_stock_ticker)+" "+symbol, Toast.LENGTH_SHORT).show();

    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    synchronized public static void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    synchronized public static void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
