package ru.vortex.geocoder.dto;

import java.util.List;

public class SearchRequest {
    private int page = 0;
    private int size = 10;
    private List<String> sort;
    private List<FilterCondition> filters;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public List<String> getSort() { return sort; }
    public void setSort(List<String> sort) { this.sort = sort; }
    public List<FilterCondition> getFilters() { return filters; }
    public void setFilters(List<FilterCondition> filters) { this.filters = filters; }

    public static class FilterCondition {
        private String field;
        private String operator;
        private Object value;

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }
}