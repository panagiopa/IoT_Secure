package com.upatras.android.iot_secure;

/**
 * Created by elite on 02-Aug-17.
 */

public class IOTData {

    private int Temperature;
    private int Humidity;
    private String CMDCounterToggle;

    IOTData() {
        this.Temperature = 0;
        this.Humidity = 0;
        this.CMDCounterToggle = "";
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

    public void setCMDCounterToggle(String s){
        this.CMDCounterToggle = s;
    }

    public String getCMDCounterToggle(){
        return  this.CMDCounterToggle;
    }

}
