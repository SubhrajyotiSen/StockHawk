package com.sam_chordas.android.stockhawk.widget;

/**
 * Created by babai on 31/5/16.
 */

public class WidgetModel {
    public String getSymbol() {
        return symbol;
    }

    public String getBid() {
        return bid;
    }

    public String getChange() {
        return change;
    }

    private String symbol;
    private String bid;
    private String change;

    public WidgetModel(String symbol, String bid, String change) {
        this.symbol = symbol;
        this.bid = bid;
        this.change = change;
    }
}
