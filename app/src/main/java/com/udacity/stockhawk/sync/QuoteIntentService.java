package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import timber.log.Timber;


public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
      //  Log.v("test2",intent.toString());
        Log.d(QuoteIntentService.class.getSimpleName(), "Stock Intent Service");
       // QuoteJobService quoteJobService=new QuoteJobService();
        //Bundle args = new Bundle();

        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
