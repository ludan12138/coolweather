package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Air;
import com.coolweather.android.gson.Daily;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Forecast_daily;
import com.coolweather.android.gson.Location_info;
import com.coolweather.android.gson.Suggestion;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.os.Build.VERSION.SDK_INT;

public class WeatherActivity extends AppCompatActivity {

    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;

    private Button navButton;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    private String c_name;

    private String c_adm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        String placeName = prefs.getString("countyName",null);
        String placeAdm = prefs.getString("adm",null);
        String suggestString = prefs.getString("suggest",null);
        String airString = prefs.getString("air",null);
        String forecastString = prefs.getString("forecast",null);
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
        //Glide.with(this).load("https://api.dujin.org/bing/m.php").into(bingPicImg);
        if(weatherString != null) {
            c_name=placeName;
            c_adm=placeAdm;
            Weather weather = Utility.handleWeatherResponse(weatherString);
            Suggestion suggest = Utility.handleSuggestResponse(suggestString);
            Air air = Utility.handleAirResponse(airString);
            Forecast forecast = Utility.handleForecastResponse(forecastString);
            showWeatherInfo(weather,placeName);
            showSuggestionInfo(suggest);
            showAirInfo(air);
            showForecastInfo(forecast);
        } else {
            String countyName = getIntent().getStringExtra("countyName");
            String adm = getIntent().getStringExtra("adm");
            c_name=countyName;
            c_adm=adm;
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(countyName,adm);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(c_name,c_adm);
            }
        });
    }
    /**according to weather id
      *to request weather info
      */
    public void requestWeather(final String countyName,final String adm) {
        c_name=countyName;
        c_adm=adm;
        String locationUrl = "https://geoapi.qweather.com/v2/city/lookup?location=" + countyName + "&adm=" + adm +"&key=d430f1787e1a433db826e6b821f986f8";
        Log.d("WeatherActivity",locationUrl);
        HttpUtil.sendOkHttpRequest(locationUrl,new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Location_info location_info = Utility.handleLocationResponse(responseText);
                String weatherId = location_info.location.get(0).id;
                String weatherUrl = "https://devapi.qweather.com/v7/weather/now?location=" + weatherId +"&key=d430f1787e1a433db826e6b821f986f8";
                HttpUtil.sendOkHttpRequest(weatherUrl,new Callback() {
                    @Override
                    public void onResponse(Call call,Response response) throws IOException {
                        final String responseText = response.body().string();
                        final Weather weather = Utility.handleWeatherResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(weather != null&& "200".equals(weather.code)) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                    editor.putString("weather",responseText);
                                    editor.putString("countyName",countyName);
                                    editor.putString("adm",adm);
                                    editor.apply();
                                    showWeatherInfo(weather,countyName);
                                } else {
                                    Toast.makeText(WeatherActivity.this,"获取天气信息失败", Toast.LENGTH_SHORT).show();
                                }
                                //swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call,IOException e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
                String suggestionUrl = "https://devapi.qweather.com/v7/indices/1d?type=1,2,8&location=" + weatherId + "&key=d430f1787e1a433db826e6b821f986f8";
                HttpUtil.sendOkHttpRequest(suggestionUrl,new Callback() {
                    @Override
                    public void onResponse(Call call,Response response) throws IOException {
                        final String responseText = response.body().string();
                        final Suggestion suggestion = Utility.handleSuggestResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if( "200".equals(suggestion.code)) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                    editor.putString("suggest",responseText);
                                    editor.apply();
                                    showSuggestionInfo(suggestion);
                                } else {
                                    Toast.makeText(WeatherActivity.this,"获取建议信息失败", Toast.LENGTH_SHORT).show();
                                }
                                //swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call,IOException e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获取建议信息失败",Toast.LENGTH_SHORT).show();
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });

                String airUrl = "https://devapi.qweather.com/v7/air/now?location=" + weatherId + "&key=d430f1787e1a433db826e6b821f986f8";
                HttpUtil.sendOkHttpRequest(airUrl,new Callback() {
                    @Override
                    public void onResponse(Call call,Response response) throws IOException {
                        final String responseText = response.body().string();
                        final Air air = Utility.handleAirResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if( "200".equals(air.code)) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                    editor.putString("air",responseText);
                                    editor.apply();
                                    showAirInfo(air);
                                } else {
                                    Toast.makeText(WeatherActivity.this,"获取空气信息失败", Toast.LENGTH_SHORT).show();
                                }
                                //swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call,IOException e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获取空气信息失败",Toast.LENGTH_SHORT).show();
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });

                String forecastUrl = "https://devapi.qweather.com/v7/weather/3d?location=" + weatherId + "&key=d430f1787e1a433db826e6b821f986f8";
                Log.d("WeatherActivity",forecastUrl);
                HttpUtil.sendOkHttpRequest(forecastUrl,new Callback() {
                    @Override
                    public void onResponse(Call call,Response response) throws IOException {
                        final String responseText = response.body().string();
                        final Forecast forecast = Utility.handleForecastResponse(responseText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if( "200".equals(forecast.code)) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                    editor.putString("forecast",responseText);
                                    editor.apply();
                                    showForecastInfo(forecast);
                                } else {
                                    Toast.makeText(WeatherActivity.this,"获取未来天气信息失败", Toast.LENGTH_SHORT).show();
                                }
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call,IOException e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获取未来天气信息失败",Toast.LENGTH_SHORT).show();
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(Call call,IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather,String countyName) {
        String cityName = countyName;
        String updateTime = weather.updateTime.split("[+|T]")[1];
        String degree = weather.now.temp+ "℃";
        String weatherInfo = weather.now.text;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void showSuggestionInfo(Suggestion suggestion) {
        for(Daily daily : suggestion.daily) {
            if(daily.type.equals("1")){
                String sport = "运动建议  " + daily.text;
                sportText.setText(sport);
            }
            if(daily.type.equals("2")){
                String carWash = "洗车指数  " + daily.text;
                carWashText.setText(carWash);
            }
            if(daily.type.equals("8")){
                String comfort = "舒适度  " + daily.text;
                comfortText.setText(comfort);
            }
        }
    }

    private void showAirInfo(Air air) {
        aqiText.setText(air.now.aqi);
        pm25Text.setText(air.now.pm2p5);
    }

    private void showForecastInfo(Forecast forecast) {
        if (forecast.daily != null) {
            for (Forecast_daily forecast_daily : forecast.daily) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
                TextView dateText = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                TextView maxText = (TextView) view.findViewById(R.id.max_text);
                TextView minText = (TextView) view.findViewById(R.id.min_text);
                dateText.setText(forecast_daily.fxDate);
                infoText.setText(forecast_daily.textDay);
                maxText.setText(forecast_daily.tempMax);
                minText.setText(forecast_daily.tempMin);
                forecastLayout.addView(view);
            }
        }
    }

    private void loadBingPic() {
        final String bingPic ="https://api.dujin.org/bing/m.php";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("bing_pic",bingPic);
        editor.apply();
        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
    }
}