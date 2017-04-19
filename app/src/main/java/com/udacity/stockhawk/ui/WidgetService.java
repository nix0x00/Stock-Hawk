package com.udacity.stockhawk.ui;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;

import timber.log.Timber;

/**
 * Created by abspk on 01/12/2016.
 */
public class WidgetService extends IntentService {

    public WidgetService() {
        super("WidgetService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Timber.d("Widget Intent");

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(getApplicationContext());

        int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for(int widgetId : widgetIds) {
            RemoteViews views = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_collection);


            Intent it = new Intent(this.getApplicationContext(), RemoteWidgetService.class);
            views.setRemoteAdapter(R.id.list_view_widget, it);
            //RemoteViews stockWidgetView = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.stock_widget);
            //stockWidgetView.setTextViewText(R.id.widget_symbol, "AAPL");


            /*Cursor cursor = getContentResolver().query(Contract.Quote.uri,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
            while(cursor.moveToNext()) {
                Log.v("DATA", "......." + cursor.getCount());
                String symbol = cursor.getString(cursor.getColumnIndexOrThrow("symbol"));
                stockWidgetView.setTextViewText(R.id.widget_symbol, symbol);
                stockWidgetView.setTextViewText(R.id.widget_price, cursor.getString(cursor.getColumnIndexOrThrow("price")));
                stockWidgetView.setTextViewText(R.id.widget_change, cursor.getString(cursor.getColumnIndexOrThrow("percentage_change")));
            }*/
            //add nested remote view
            //views.addView(R.id.widget, stockWidgetView);

            //launch MainActivity
            Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, newIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

            //set Empty view if no data
            views.setEmptyView(R.id.widget_layout, R.id.empty_msg_view);

            widgetManager.updateAppWidget(widgetId, views);
        }
    }
}
