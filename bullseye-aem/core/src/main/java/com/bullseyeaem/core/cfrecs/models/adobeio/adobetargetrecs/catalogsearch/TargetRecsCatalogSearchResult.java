package com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch;

import java.util.List;
import java.util.Map;

public class TargetRecsCatalogSearchResult {
    private List<Map<String,?>> hits;
    private Integer timeMs;
    private Integer start;

    public List<Map<String, ?>> getHits() {
        return hits;
    }

    public Integer getTimeMs() {
        return timeMs;
    }

    public Integer getStart() {
        return start;
    }
}
