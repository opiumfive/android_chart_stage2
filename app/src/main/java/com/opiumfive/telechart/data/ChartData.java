package com.opiumfive.telechart.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartData implements Parcelable {

    private List<ColumnData> columns;
    private Map<String, String> types;
    private Map<String, String> names;
    private Map<String, String> colors;

    public List<ColumnData> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnData> columns) {
        this.columns = columns;
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public Map<String, String> getColors() {
        return colors;
    }

    public void setColors(Map<String, String> colors) {
        this.colors = colors;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.columns);
        dest.writeInt(this.types.size());
        for (Map.Entry<String, String> entry : this.types.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeInt(this.names.size());
        for (Map.Entry<String, String> entry : this.names.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeInt(this.colors.size());
        for (Map.Entry<String, String> entry : this.colors.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    public ChartData() {
    }

    protected ChartData(Parcel in) {
        this.columns = in.createTypedArrayList(ColumnData.CREATOR);
        int typesSize = in.readInt();
        this.types = new HashMap<String, String>(typesSize);
        for (int i = 0; i < typesSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.types.put(key, value);
        }
        int namesSize = in.readInt();
        this.names = new HashMap<String, String>(namesSize);
        for (int i = 0; i < namesSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.names.put(key, value);
        }
        int colorsSize = in.readInt();
        this.colors = new HashMap<String, String>(colorsSize);
        for (int i = 0; i < colorsSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.colors.put(key, value);
        }
    }

    public static final Creator<ChartData> CREATOR = new Creator<ChartData>() {
        @Override
        public ChartData createFromParcel(Parcel source) {
            return new ChartData(source);
        }

        @Override
        public ChartData[] newArray(int size) {
            return new ChartData[size];
        }
    };
}
