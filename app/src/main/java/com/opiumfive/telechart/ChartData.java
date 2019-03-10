package com.opiumfive.telechart;

import java.util.List;
import java.util.Map;

public class ChartData {

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
}
