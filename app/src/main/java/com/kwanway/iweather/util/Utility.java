package com.kwanway.iweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.kwanway.iweather.db.City;
import com.kwanway.iweather.db.County;
import com.kwanway.iweather.db.Provice;
import com.kwanway.iweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    /**
      * Parse the provice data returned.
      */
    public static boolean handleProviceResponse(String response) {

        if (!TextUtils.isEmpty(response)) {
            try {

                JSONArray allProvices = new JSONArray(response);
                for (int i = 0; i < allProvices.length(); i++) {
                    JSONObject proviceObject = allProvices.getJSONObject(i);

                    Provice provice = new Provice();
                    provice.setProviceName(proviceObject.getString("name"));
                    provice.setProviceCode(proviceObject.getInt("id"));
                    provice.save();     // store provice in database
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Parse the city data returned.
     */
    public static boolean handleCityResponse(String response, int provinceId) {

        if (!TextUtils.isEmpty(response)) {
            try {

                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProviceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Parse the county data returned.
     */
    public static boolean handleCountyResponse(String response, int cityId) {

        if (!TextUtils.isEmpty(response)) {
            try {

                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {

                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Parse JSON data to weather instance
     */
    public static Weather handleWeatherResponse(String response) {

        try {

            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
