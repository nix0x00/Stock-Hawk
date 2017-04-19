package com.udacity.stockhawk.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.NumberFormat;
import java.util.Locale;
import java.text.DecimalFormat;

/**
 * Created by abspk on 02/12/2016.
 */

public class RemoteWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        return new RemoteViewsFactory() {
            private Cursor cursor = null;
            private String symbol = "";
            private DecimalFormat decimalFormat;
            private DecimalFormat decimalPrice;

            @Override
            public void onCreate() {
                Log.v("WIDGET", "............onCreate");
                decimalPrice = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                decimalFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                decimalPrice.setPositivePrefix("+$");
                decimalFormat.setPositivePrefix("+");
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                cursor = getContentResolver().query(Contract.Quote.uri,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int i) {
                if (i == AdapterView.INVALID_POSITION || cursor == null) {
                    return null;
                }

                Log.v("WIDGET", "............I HAVE BEEN SUMMONED");

                RemoteViews stockWidgetView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.stock_widget);

                if (cursor.moveToPosition(i)) {

                    symbol = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Quote.COLUMN_SYMBOL));
                    stockWidgetView.setTextViewText(R.id.widget_symbol, cursor.getString(Contract.Quote.POSITION_SYMBOL));
                    stockWidgetView.setTextViewText(R.id.widget_price, cursor.getString(Contract.Quote.POSITION_PRICE));

                    stockWidgetView.setTextViewText(R.id.widget_change, cursor.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE));

                }


                /*
                This is not working. I want that when clicked on widget, it should launch the graph
                of that particular stock, but it doesn't.
                 */
                Intent it = new Intent(getApplicationContext(), GraphActivity.class);
                it.putExtra("data", symbol);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, it, 0);

                stockWidgetView.setOnClickPendingIntent(R.layout.stock_widget, pi);

                return stockWidgetView;
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
            public long getItemId(int i) {
                if (cursor.moveToPosition(i)) {
                    return cursor.getLong(cursor.getColumnIndexOrThrow(Contract.Quote._ID));
                }
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}