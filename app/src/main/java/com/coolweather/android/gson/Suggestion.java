package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Suggestion {

    public String code;

    public String updateTime;

    public String fxLink;

    public List<Daily> daily;

    public Refer refer;
}
