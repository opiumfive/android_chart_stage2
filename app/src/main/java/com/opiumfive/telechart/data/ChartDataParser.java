package com.opiumfive.telechart.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChartDataParser {

    private static final String CHART_DATA_NAME = "chart_data.json";

    @Nullable
    public static ChartData loadAndParseInput(@NonNull Context context, int pos) {
        String json = getDataFromJson(context);
        if (TextUtils.isEmpty(json)) {
            return null;
        } else {
            return parseData(json, pos);
        }
    }

    @Nullable
    private static String getDataFromJson(@NonNull Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(CHART_DATA_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static ChartData parseData(@NonNull String json, int position) {
        ChartData chartData = new ChartData();

        try {
            JSONArray obj = new JSONArray(json);
            JSONObject chart1 = obj.getJSONObject(position);

            JSONArray columns = chart1.getJSONArray("columns");
            JSONObject types = chart1.getJSONObject("types");
            JSONObject names = chart1.getJSONObject("names");
            JSONObject colors = chart1.getJSONObject("colors");

            List<ColumnData> columnsList = new ArrayList<>(columns.length());

            for (int i = 0; i < columns.length(); i++) {
                JSONArray column = columns.getJSONArray(i);
                ColumnData columnData = new ColumnData();
                columnData.setTitle(column.getString(0));
                List<Long> valuesList = new ArrayList<>(column.length() - 1);
                for (int j = 1; j < column.length(); j++) {
                    valuesList.add(column.getLong(j));
                }
                columnData.setList(valuesList);
                columnsList.add(columnData);
            }

            Map<String, String> typesMap = new HashMap<>(types.names().length());
            for(Iterator<String> keys = types.keys(); keys.hasNext();) {
                String key = keys.next();
                typesMap.put(key, types.getString(key));
            }

            Map<String, String> namesMap = new HashMap<>(names.names().length());
            for(Iterator<String> keys = names.keys(); keys.hasNext();) {
                String key = keys.next();
                namesMap.put(key, names.getString(key));
            }

            Map<String, String> colorsMap = new HashMap<>(colors.names().length());
            for(Iterator<String> keys = colors.keys(); keys.hasNext();) {
                String key = keys.next();
                colorsMap.put(key, colors.getString(key));
            }

            chartData.setColors(colorsMap);
            chartData.setNames(namesMap);
            chartData.setTypes(typesMap);
            chartData.setColumns(columnsList);
        } catch (JSONException e) {
            e.printStackTrace();
            chartData = null;
        }

        return chartData;
    }
}
