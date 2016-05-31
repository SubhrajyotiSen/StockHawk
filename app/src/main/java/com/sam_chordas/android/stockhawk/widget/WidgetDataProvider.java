package com.sam_chordas.android.stockhawk.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("NewApi")
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    ArrayList<WidgetModel> mCollections;
    Cursor cursor;

    Context mContext = null;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        mCollections = new ArrayList();
    }

    @Override
    public int getCount() {
        return mCollections.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_item);
        /*mView.setTextViewText(R.id.stock_symbol, (CharSequence) mCollections.get(position));
        mView.setTextColor(R.id.stock_symbol, Color.WHITE);*/
            mView.setTextViewText(R.id.stock_symbol,
                    mCollections.get(position).getSymbol());
            mView.setTextViewText(R.id.bid_price,
                    mCollections.get(position).getBid());
            mView.setTextViewText(R.id.stock_change,
                    mCollections.get(position).getChange());

        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        mCollections.clear();
        cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        while (cursor.moveToNext()){
            mCollections.add(new WidgetModel(cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)),cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)),cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE))));
        }
    }

    @Override
    public void onDestroy() {

    }

}