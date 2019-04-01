package com.opiumfive.telechart.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.opiumfive.telechart.chart.model.Viewrect;

public class State implements Parcelable {

    private Viewrect currentViewrect;
    private boolean[] linesState;

    public State(Viewrect currentViewrect, boolean[] linesState) {
        this.currentViewrect = currentViewrect;
        this.linesState = linesState;
    }

    public Viewrect getCurrentViewrect() {
        return currentViewrect;
    }

    public boolean[] getLinesState() {
        return linesState;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.currentViewrect, flags);
        dest.writeBooleanArray(this.linesState);
    }

    protected State(Parcel in) {
        this.currentViewrect = in.readParcelable(Viewrect.class.getClassLoader());
        this.linesState = in.createBooleanArray();
    }

    public static final Creator<State> CREATOR = new Creator<State>() {
        @Override
        public State createFromParcel(Parcel source) {
            return new State(source);
        }

        @Override
        public State[] newArray(int size) {
            return new State[size];
        }
    };
}
