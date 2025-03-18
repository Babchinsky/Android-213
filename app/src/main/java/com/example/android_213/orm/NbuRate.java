package com.example.android_213.orm;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NbuRate {
    public static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT);
    private int r030;
    private String text;
    private double rate;
    private String cc;
    private Date exchangeDate;

    public static NbuRate fromJsonObject( JSONObject jsonObject) throws JSONException {
        NbuRate nbuRate = new NbuRate();
        nbuRate.setR030(jsonObject.getInt("r030"));
        nbuRate.setText(jsonObject.getString("txt"));
        nbuRate.setCc(jsonObject.getString("cc"));
        nbuRate.setRate(jsonObject.getDouble("rate"));
        try {

            nbuRate.setExchangeDate(
                    dateFormat.parse(
                            jsonObject.getString("exchangedate")
                    )
            );
        }
        catch(ParseException ex){
            throw new JSONException(ex.getMessage() );
        }
        return  nbuRate;
    }

    public int getR030() {
        return r030;
    }

    public void setR030(int r030) {
        this.r030 = r030;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public Date getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(Date exchangeDate) {
        this.exchangeDate = exchangeDate;
    }
}