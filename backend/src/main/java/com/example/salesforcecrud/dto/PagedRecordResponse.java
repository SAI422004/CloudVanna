package com.example.salesforcecrud.dto;

import java.util.List;
import java.util.Map;

/**
 * Paginated response wrapping Salesforce records and cursor info.
 */
public class PagedRecordResponse {

    private List<Map<String, Object>> records;
    private int totalSize;
    private boolean done;
    private String nextPageUrl;

    public PagedRecordResponse() {}

    public PagedRecordResponse(List<Map<String, Object>> records, int totalSize, boolean done, String nextPageUrl) {
        this.records = records;
        this.totalSize = totalSize;
        this.done = done;
        this.nextPageUrl = nextPageUrl;
    }

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }
}
