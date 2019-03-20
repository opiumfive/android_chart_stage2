package com.opiumfive.telechart.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ColumnData implements Parcelable {

    private String title;
    private List<Long> list;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Long> getList() {
        return list;
    }

    public void setList(List<Long> list) {
        this.list = list;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeList(this.list);
    }

    public ColumnData() {
    }

    protected ColumnData(Parcel in) {
        this.title = in.readString();
        this.list = new ArrayList<Long>();
        in.readList(this.list, Long.class.getClassLoader());
    }

    public static final Creator<ColumnData> CREATOR = new Creator<ColumnData>() {
        @Override
        public ColumnData createFromParcel(Parcel source) {
            return new ColumnData(source);
        }

        @Override
        public ColumnData[] newArray(int size) {
            return new ColumnData[size];
        }
    };
}
