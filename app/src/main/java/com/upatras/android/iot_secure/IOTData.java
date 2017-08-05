package com.upatras.android.iot_secure;

/**
 * Created by elite on 02-Aug-17.
 */

public class IOTData {

    private int Temperature;
    private int Humidity;
    private String CMDCounter;
    private String CMDResponse;
    private boolean isSent;

    IOTData() {
        this.Temperature = 0;
        this.Humidity = 0;
        this.CMDCounter = "";
        this.CMDResponse = "";
        isSent = false;
    }

    public void setTemperature(int s) {
        this.Temperature = s;
    }

    public int getTemperature() {
        return  this.Temperature;
    }

    public void setHumidity(int s){
        this.Humidity = s;
    }

    public int getHumidity(){
        return this.Humidity;
    }

    public void setCMDCounter(String s){
        this.CMDCounter = s;
    }

    public String getCMDCounter(){
        return  this.CMDCounter;
    }

    public String getCMDResponse() {
        return this.CMDResponse;
    }

    public void setCMDResponse(String s){
        this.CMDResponse = s;
    }

    public boolean getisSent()
    {
        return this.isSent;
    }

    public  void setisSent(boolean s)
    {
        this.isSent = s;
    }

}
