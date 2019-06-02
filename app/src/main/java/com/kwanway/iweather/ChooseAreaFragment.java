package com.kwanway.iweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kwanway.iweather.db.City;
import com.kwanway.iweather.db.County;
import com.kwanway.iweather.db.Provice;
import com.kwanway.iweather.gson.Weather;
import com.kwanway.iweather.util.HttpUtil;
import com.kwanway.iweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVICE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    /**
     * Provice list
     */
    private List<Provice> proviceList;

    /**
     * City list
     */
    private List<City> cityList;

    /**
     * County list
     */
    private List<County> countyList;

    /**
     * Selected provice
     */
    private Provice selectedProvice;

    /**
     * Selected city
     */
    private City selectedCity;

    /**
     * Current selected level
     */
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false);

        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentLevel == LEVEL_PROVICE) {
                    selectedProvice = proviceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvices();
                }
            }
        });

        queryProvices();
    }

    /**
     * Check all provices, check database first, if not exist, check server
     */
    private void queryProvices() {

        titleText.setText("China");
        backButton.setVisibility(View.GONE);
        proviceList = DataSupport.findAll(Provice.class);

        if (proviceList.size() > 0) {
            dataList.clear();
            for (Provice provice: proviceList) {
                dataList.add(provice.getProviceName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVICE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "provice");
        }
    }

    /**
     * Check all cities of selected provice, check database first, if not exist, check server.
     */
    private void queryCities() {

        titleText.setText(selectedProvice.getProviceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("proviceid = ?",
                String.valueOf(selectedProvice.getId())).find(City.class);

        if (cityList.size() > 0) {
            dataList.clear();

            for (City city: cityList) {
                dataList.add(city.getCityName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int proviceCode = selectedProvice.getProviceCode();
            String address = "http:guolin.tech/api/china/" + proviceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * Check all couties of selected city, check database first, if not exist, check server.
     */
    private void queryCounties() {

        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",
                String.valueOf(selectedCity.getId())).find(County.class);

        if (countyList.size() > 0) {
            dataList.clear();
            for (County county: countyList) {
                dataList.add(county.getCountyName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int proviceCode = selectedProvice.getProviceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + proviceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * query provice, city, county data from server using address and type
     * @param address: server address
     * @param type: provice, city, count
     */
    private void queryFromServer(String address, final String type) {

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "Load data failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText = response.body().string();
                boolean result = false;

                if ("provice".equals(type)) {

                    result = Utility.handleProviceResponse(responseText);
                } else if ("city".equals(type)) {

                    result = Utility.handleCityResponse(responseText, selectedProvice.getId());
                } else if ("county".equals(type)) {

                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();

                            if ("provice".equals(type)) {

                                queryProvices();
                            } else if ("city".equals(type)) {

                                queryCities();
                            } else if ("county".equals(type)) {

                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

    }

    private void closeProgressDialog() {

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
