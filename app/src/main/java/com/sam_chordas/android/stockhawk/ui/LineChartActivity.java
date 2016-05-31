package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class LineChartActivity extends AppCompatActivity {

    private static String LOG_TAG = "LineChartActivity";

    private LineChartView chart;
    private TextView emptyView;
    private Float high;
    private Float low;
    private String symbol;
    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_line_chart);
        chart = (LineChartView) findViewById(R.id.linechart);
        assert chart != null;
        chart.setVisibility(View.VISIBLE);
        emptyView = (TextView) findViewById(R.id.chart_emptyView);
        assert emptyView != null;
        emptyView.setVisibility(View.GONE);

        high=0.00f;
        low=999999.00f;

        if(getIntent().getExtras().containsKey(QuoteColumns.SYMBOL)){
            symbol=getIntent().getExtras().getString(QuoteColumns.SYMBOL);
            name=getIntent().getExtras().getString(QuoteColumns.NAME);
            new StockChartAsyncTask().execute(symbol, null);
            assert getSupportActionBar()!=null;
            getSupportActionBar().setTitle(name.toUpperCase() );

        } else {

            chart.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private class StockChartAsyncTask extends AsyncTask<String, Integer, LineSet> {
        @Override
        protected LineSet doInBackground(String... params) {
            //Just going to query for the passed symbol parameter
            StringBuilder urlStringBuilder = new StringBuilder();
            if (params==null){
                return null; }
            if(params.length<1){
                return null;
            }

            try{
                //Base URL
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = \"", "UTF-8"));
                urlStringBuilder.append(params[0]);
                //Add initial date argument, which will just be 3 months before today
                urlStringBuilder.append(URLEncoder.encode("\" and startDate = \"", "UTF-8"));

                //Get our start and end dates
                Calendar c = Calendar.getInstance();
                Date now = c.getTime();
                c.add(Calendar.MONTH, -2);
                Date threeMonthsAgo = c.getTime();

                //Append Date Parameters
                urlStringBuilder.append(getFormattedDate(threeMonthsAgo));
                urlStringBuilder.append(URLEncoder.encode("\" and endDate = \"", "UTF-8"));
                urlStringBuilder.append(getFormattedDate(now));
                urlStringBuilder.append("\"&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String getResponse;

            try{
                getResponse=fetchData(urlStringBuilder.toString());
                return parseJSONtoPoints(getResponse);

            } catch (IOException e){
                e.printStackTrace();
            }


            return null;
        }
        protected void onPostExecute(LineSet result){
            if(result!=null){
                setUpChart(result);

            } else {
                chart.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }

        }
    }

    private LineSet parseJSONtoPoints(String jsonResults){
        JSONArray resultsArray = null;
        LineSet set = new LineSet();

        int resultCount = 0;
        try{
            JSONObject jsonObject = new JSONObject(jsonResults);
            if (jsonObject.length() != 0){
                jsonObject = jsonObject.getJSONObject("query");

                //Grab a count, make sure its more than one and return a JSONArray
                resultCount = Integer.parseInt(jsonObject.getString("count"));
                Log.e(LOG_TAG, String.valueOf(resultCount));
                if(resultCount>=1){
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                }
            }

        }catch (JSONException e){
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }


        JSONObject point;
        Float close;
        //If we got results, convert them to Strings and Ints, then Points for Graph use
        if(resultCount>0) {
            for (int i = 0; i <= resultCount-1; i++) {
                try{
                    assert resultsArray != null;
                    point = resultsArray.getJSONObject(i);
                    close=Float.valueOf(point.getString("Open"));
                    if(close>high){
                        high=close;
                    }

                    if(close<low){
                        low=close;
                    }
                    set.addPoint(point.getString("Date"), close);
                } catch(JSONException e){
                    Log.e(LOG_TAG, "Conversion of JSONARRAY to Object failed:" + e);
                }
            }

            return set;
        }

        return null;
    }

    private String getFormattedDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }


    private String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void setUpChart(LineSet result){
        result.setColor(ContextCompat.getColor(this, R.color.material_green_700));
        chart.addData(result);
        chart.setAxisColor(Color.WHITE);
        chart.setLabelsColor(Color.WHITE);
        chart.setXLabels(AxisController.LabelPosition.NONE);
        chart.setAxisBorderValues(Math.round(low), Math.round(high), Math.round(Math.round((1.2 * high) / (.8 * low))));
        chart.show();
    }

}
