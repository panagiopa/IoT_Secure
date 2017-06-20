package com.upatras.android.iot_secure;

import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by elite on 18/6/2017.
 */

public class AjaxReq {

    private AQuery aq;
    private String[] retObj = null;
    AjaxReq(AQuery aq)
    {
        this.aq = aq;
    }

    public void AjaxGetRequest(String url)
    {
        //String url = "http://192.168.10.4/test.php?id=1";
        //Make Asynchronous call using AJAX method
        Log.d("TEST",url);
        aq.ajax(url, JSONObject.class, this, "jsonCallback");
    }

    public String[] getRetObj()
    {
        return retObj;
    }

    public void jsonCallback(String url, JSONObject json, AjaxStatus status){

        Log.d("TEST","return");

        if (json != null) {
            retObj = null;
            Gson gson = new GsonBuilder().create();
            try {
                //Get JSON response by converting JSONArray into String
                String jsonResponse = json.getJSONArray("Cities").toString();
                Log.d("TEST",jsonResponse);
                //Using fromJson method deserialize JSON response [Convert JSON array into Java array]
                retObj = gson.fromJson(jsonResponse, String[].class);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Toast.makeText(aq.getContext(), "Error in parsing JSON", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(aq.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
        //When JSON is null
        else {
            //When response code is 500 (Internal Server Error)
            if(status.getCode() == 500){
                Toast.makeText(aq.getContext(),"Server is busy or down. Try again!",Toast.LENGTH_SHORT).show();
            }
            //When response code is 404 (Not found)
            else if(status.getCode() == 404){
                Toast.makeText(aq.getContext(),"Resource not found!",Toast.LENGTH_SHORT).show();
            }
            //When response code is other 500 or 404
            else{
                Toast.makeText(aq.getContext(),"Unexpected Error occurred",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public  void test()
    {
        String url = "http://search.twitter.com/search.json";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("q", "androidquery");
        Log.d("POST",url);
        aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {

                Log.d("POST",json.toString());

            }
        });
    }
}
