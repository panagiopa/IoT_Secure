package com.upatras.android.iot_secure;

/**
 * Created by elite on 02-Aug-17.
 */

public class IOTData {

    private int Temperature;
    private int Humidity;
    private int CMDCounterToggle;

    IOTData() {
        this.Temperature = 0;
        this.Humidity = 0;
        this.CMDCounterToggle = 0;
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

    public void setCMDCounterToggle(int s){
        this.CMDCounterToggle = s;
    }

    public int getCMDCounterToggle(){
        return  this.CMDCounterToggle;
    }

}
