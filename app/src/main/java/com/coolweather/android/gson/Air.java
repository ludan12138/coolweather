package com.coolweather.android.gson;

public class Air {
    public String code;

    public String updateTime;

    public String fxLink;

    public Now_air now;

    public class Now_air{
        public String pubTime;
        public String aqi;
        public String level;
        public String category;
        public String primary;
        public String pm10;
        public String pm2p5;
        public String no2;
        public String so2;
        public String co;
        public String o3;
    }
}
