package com.opiumfive.telechart.data;

import android.content.Context;
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

import static com.opiumfive.telechart.Settings.CHART_COUNT;
import static com.opiumfive.telechart.Settings.CHART_DATA_NAME;

public class ChartDataParser {

    public static List<ChartData> loadAndParseInput(Context context) {
        List<ChartData> result = new ArrayList<>(CHART_COUNT);
        for (int i = 1; i <= CHART_COUNT; i++) {
            String json = getDataFromJson(context, i + "/");
            result.add(parseData(json));
        }

        return result;
    }

    private static String getDataFromJson(Context context, String path) {
        try {
            InputStream is = context.getAssets().open(path + CHART_DATA_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytes = is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static ChartData parseData(String json) {
        try {
            JSONObject chart = new JSONObject(json);

            ChartData chartData = new ChartData();

            JSONArray columns = chart.getJSONArray("columns");
            JSONObject types = chart.getJSONObject("types");
            JSONObject names = chart.getJSONObject("names");
            JSONObject colors = chart.getJSONObject("colors");

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

            return chartData;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
