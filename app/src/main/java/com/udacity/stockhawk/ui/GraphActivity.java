package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abspk on 01/12/2016.
 */

public class GraphActivity extends AppCompatActivity {

    private Cursor cursor;
    String change, symbol, bid, changeInPercent;
    List<String> close;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_graph);

        String symbl = getIntent().getStringExtra("data");

        cursor = getContentResolver().query(Contract.Quote.uri,
                new String[]{Contract.Quote.COLUMN_PRICE},
                Contract.Quote.COLUMN_SYMBOL + " = ?", new String[]{symbl}, null);

        get(symbl);
        //setGraph();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setGraph() {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] data = new DataPoint[close.size()];

        for (int i = 0; i < close.size(); i++) {
            DataPoint a = new DataPoint(i+1, Float.parseFloat(close.get(i)));
            data[i] = a;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(data);

        graph.addSeries(series);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
        }

        return true;
    }

    private void get(String symbol) {
        //String str = "https://query.yahooapis.com/v1/public/yql?q=select+*+from+yahoo.finance.quotes+where+symbol+in+(\"" + symbol + "\")&format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys&callback=\n";
        String base = "https://query.yahooapis.com/v1/public/yql?q=";

        String url = "select * from yahoo.finance.historicaldata where symbol = '" + symbol + "' and startDate = '2016-01-01' and endDate = '2016-01-10'";
        String query = "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
        //url = base.concat(url).concat(query);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(base).append(URLEncoder.encode(url)).append(query);

        Log.v("URL", "............." + stringBuilder.toString());

        try {

            StringRequest stringRequest = new StringRequest(Request.Method.GET,
                    stringBuilder.toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                   // Log.v("DATA", "..............    " + response.toString());

                    try {
                        parseJason(response);
                    } catch (JSONException x) {
                        x.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError er) {
                    er.printStackTrace();
                }
            });

            RequestQueue rq = Volley.newRequestQueue(this);
            rq.add(stringRequest);

            //Log.v("DATA", "......." + stringRequest.toString());
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private void parseJason(String str) throws JSONException {

        JSONObject obj = new JSONObject(str);
        obj = obj.getJSONObject("query");

        int count = obj.getInt("count");
        //if (count == 1) {
            obj = obj.getJSONObject("results");

            JSONArray array = obj.getJSONArray("quote");
            close = new ArrayList<String>();

            for (int i = 0; i < array.length(); i++) {
                obj = array.getJSONObject(i);
                bid = obj.getString("Close");
                //bid = formatChangeInPercent(bid, true);
                Log.v("BID", ".................." + bid);
                close.add(bid);
                setGraph();
            }
        //}
    }

    //this algorithm was taken from github by some vycis user
    public String formatChangeInPercent(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }
}
