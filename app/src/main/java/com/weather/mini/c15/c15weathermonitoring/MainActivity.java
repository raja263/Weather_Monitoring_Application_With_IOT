package com.weather.mini.c15.c15weathermonitoring;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    ProgressBar freezeBar,heatBar,humidityBar;
    TextView temperatureTV,humidityTV;
    int graphUpdater=0,temperatureValue=0,humidityValue=0;
    String result="",tempJSON,humidityJSON, temperatureGraphURL,humidityGraphURL;
    WebView tempGraph,humidityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        freezeBar = (ProgressBar)findViewById(R.id.progressBar); freezeBar.setProgress(0);
        heatBar = (ProgressBar)findViewById(R.id.progressBar2); heatBar.setProgress(0);
        humidityBar = (ProgressBar)findViewById(R.id.progressBarHumidity); humidityBar.setProgress(0);

        temperatureTV = (TextView)findViewById(R.id.temperature);
        humidityTV = (TextView)findViewById(R.id.Humidity);

        String url = "https://api.thingspeak.com/channels/503679/feeds/last.json?api_key=";
        String apikey = "GO9R3WV4X1EI29X9";
        final UriApi uriapi01 = new UriApi();


        uriapi01.setUri(url,apikey);
        Timer timer = new Timer();
        TimerTask tasknew = new TimerTask(){
            public void run() {
                LoadJSON task = new LoadJSON();
                task.execute(uriapi01.getUri());
            }
        };
        timer.scheduleAtFixedRate(tasknew,1*1000,1*1000);

        // TEMPERATURE GRAPH
         temperatureGraphURL = "<iframe width=\"450\" height=\"250\" style=\"border: 1px solid #cccccc;\" src=\"http://thingspeak.com/channels/503679/charts/1?api_key=GO9R3WV4X1EI29X9&dynamic=true\"></iframe>";
         tempGraph = (WebView) findViewById(R.id.tempGraph);
        tempGraph.getSettings().setJavaScriptEnabled(true);
        tempGraph.setInitialScale(210);
        tempGraph.loadData(temperatureGraphURL, "text/html", null);
        // HUMIDITY GRAPH
         humidityGraphURL = "<iframe width=\"450\" height=\"250\" style=\"border: 1px solid #cccccc;\" src=\"http://thingspeak.com/channels/503679/charts/2?api_key=GO9R3WV4X1EI29X9&dynamic=true\"></iframe>";
         humidityGraph = (WebView) findViewById(R.id.humidityGraph);
        humidityGraph.getSettings().setJavaScriptEnabled(true);
        humidityGraph.setInitialScale(210);
        humidityGraph.loadData(humidityGraphURL, "text/html", null);


    }

    private class UriApi {

        private String uri,url,apikey;

        protected void setUri(String url, String apikey){
            this.url = url;
            this.apikey = apikey;
            this.uri = url + apikey;
        }

        protected  String getUri(){
            return uri;
        }

    }

    private class LoadJSON extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return getText(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {


            try {
                JSONObject json = new JSONObject(result);

                tempJSON = String.format("%s", json.getString("field1"));
                humidityJSON = String.format("%s", json.getString("field2"));
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
            graphUpdater++;
            temperatureTV.setText(""+tempJSON+"°C");
            Log.d("VarX", ""+tempJSON);
            humidityTV.setText(""+humidityJSON+"%");
            Log.d("VarY",""+humidityJSON);

            if(graphUpdater==5)  //to update graphs every 5 seconds
            {
                tempGraph.loadData(temperatureGraphURL, "text/html", null);  //updates temperature graph
                humidityGraph.loadData(humidityGraphURL, "text/html", null); //updates humidity graph
                graphUpdater=0;
            }


            try
            {   if(tempJSON!=null) {
                temperatureValue = Integer.parseInt(tempJSON);
                }
            }
            catch(NumberFormatException nfe){}

            try
            {
                humidityValue = Integer.parseInt(humidityJSON);
            }
            catch(NumberFormatException nfe){}

            //Temperature Progress Bar Code --------------------------------------------------------
            if(temperatureValue==0)
            {
                heatBar.setProgress(0);

                Drawable bgDrawable = heatBar.getProgressDrawable();
                bgDrawable.setColorFilter(Color.parseColor("#FFA4EEFA"), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            else if(temperatureValue>0)
            {

                heatBar.setProgress(temperatureValue);
                freezeBar.setProgress(0);

                Drawable bgDrawable = heatBar.getProgressDrawable();
                Drawable bgDrawable1 = freezeBar.getProgressDrawable();
                bgDrawable1.setColorFilter(Color.parseColor("#00ddff"), android.graphics.PorterDuff.Mode.MULTIPLY);

                if(temperatureValue<20)  {bgDrawable.setColorFilter(Color.parseColor("#00fab3"), android.graphics.PorterDuff.Mode.MULTIPLY); }
                if(temperatureValue>20)  {bgDrawable.setColorFilter(Color.parseColor("#0cfb4c"), android.graphics.PorterDuff.Mode.MULTIPLY);}
                if(temperatureValue>40)  {bgDrawable.setColorFilter(Color.parseColor("#ff9500"), android.graphics.PorterDuff.Mode.MULTIPLY); }
                if(temperatureValue>60)  {bgDrawable.setColorFilter(Color.parseColor("#eb4e01"), android.graphics.PorterDuff.Mode.MULTIPLY); }
                if(temperatureValue>80)  {bgDrawable.setColorFilter(Color.parseColor("#ff0400"), android.graphics.PorterDuff.Mode.MULTIPLY); }
            }
            else if(temperatureValue<0)
            {

                freezeBar.setProgress(temperatureValue);
                heatBar.setProgress(0);

                Drawable bgDrawable = freezeBar.getProgressDrawable();
                Drawable bgDrawable1 = heatBar.getProgressDrawable();
                bgDrawable1.setColorFilter(Color.parseColor("#ff9500"), android.graphics.PorterDuff.Mode.MULTIPLY);

                if(temperatureValue>-20) {bgDrawable.setColorFilter(Color.parseColor("#00ddff"), android.graphics.PorterDuff.Mode.MULTIPLY); }
                if(temperatureValue<-20) {bgDrawable.setColorFilter(Color.parseColor("#0091ca"), android.graphics.PorterDuff.Mode.MULTIPLY); }
                if(temperatureValue<-40) {bgDrawable.setColorFilter(Color.parseColor("#006dc6"), android.graphics.PorterDuff.Mode.MULTIPLY); }
                if(temperatureValue<-60) {bgDrawable.setColorFilter(Color.parseColor("#8f05ff"), android.graphics.PorterDuff.Mode.MULTIPLY); }
                if(temperatureValue<-80) {bgDrawable.setColorFilter(Color.parseColor("#7100c2"), android.graphics.PorterDuff.Mode.MULTIPLY); }
            }

            //Humidity Progress Bar Code -----------------------------------------------------------
            Drawable bgDrawable = humidityBar.getProgressDrawable();
            humidityBar.setProgress(humidityValue*3);
            Log.d("humidity:","X");
            if(humidityValue>=0&&humidityValue<40) {bgDrawable.setColorFilter(Color.parseColor("#8ff7f1"), android.graphics.PorterDuff.Mode.MULTIPLY); }
            if(humidityValue>40&&humidityValue<60) {bgDrawable.setColorFilter(Color.parseColor("#59ff49"), android.graphics.PorterDuff.Mode.MULTIPLY); }
            if(humidityValue>60&&humidityValue<80) {bgDrawable.setColorFilter(Color.parseColor("#e0ea2a"), android.graphics.PorterDuff.Mode.MULTIPLY); }
            if(humidityValue>=80) {bgDrawable.setColorFilter(Color.parseColor("#ea662a"), android.graphics.PorterDuff.Mode.MULTIPLY); }

        }
    }

    private String getText(String strUrl) {
        String strResult = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            strResult = readStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strResult;
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
