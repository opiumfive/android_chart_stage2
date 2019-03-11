package com.opiumfive.telechart.chart.model;


public class SelectedValue {

    private int firstIndex;

    private int secondIndex;

    private SelectedValueType type = SelectedValueType.NONE;

    public SelectedValue() {
        clear();
    }

    public SelectedValue(int firstIndex, int secondIndex, SelectedValueType type) {
        set(firstIndex, secondIndex, type);
    }

    public void set(int firstIndex, int secondIndex, SelectedValueType type) {
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        if (null != type) {
            this.type = type;
        } else {
            this.type = SelectedValueType.NONE;
        }
    }

    public void set(SelectedValue selectedValue) {
        this.firstIndex = selectedValue.firstIndex;
        this.secondIndex = selectedValue.secondIndex;
        this.type = selectedValue.type;
    }

    public void clear() {
        set(Integer.MIN_VALUE, Integer.MIN_VALUE, SelectedValueType.NONE);
    }

    public boolean isSet() {
        if (firstIndex >= 0 && secondIndex >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getSecondIndex() {
        return secondIndex;
    }

    public void setSecondIndex(int secondIndex) {
        this.secondIndex = secondIndex;
    }

    public SelectedValueType getType() {
        return type;
    }

    public void setType(SelectedValueType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + firstIndex;
        result = prime * result + secondIndex;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SelectedValue other = (SelectedValue) obj;
        if (firstIndex != other.firstIndex)
            return false;
        if (secondIndex != other.secondIndex)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SelectedValue [firstIndex=" + firstIndex + ", secondIndex=" + secondIndex + ", type=" + type + "]";
    }

    public enum SelectedValueType {
        NONE, LINE
    }

}
