package com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs;

import java.math.BigDecimal;

public class TargetRecsEntityAttributes {
    public static final String MESSAGE = "message";
    public static final String THUMBNAIL_URL = "thumbnailUrl";
    public static final String VALUE = "value";
    public static final String PAGE_URL = "pageUrl";
    public static final String INVENTORY = "inventory";
    public static final String MARGIN = "margin";

    private final String message;
    private final String thumbnailUrl;
    private final BigDecimal value;
    private final String pageUrl;
    private final Integer inventory;
    private final BigDecimal margin;

    public TargetRecsEntityAttributes(String message, String thumbnailUrl, BigDecimal value, String pageUrl, Integer inventory, BigDecimal margin) {
        this.message = message;
        this.thumbnailUrl = thumbnailUrl;
        this.value = value;
        this.pageUrl = pageUrl;
        this.inventory = inventory;
        this.margin = margin;
    }

    public String getMessage() {
        return message;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public Integer getInventory() {
        return inventory;
    }

    public BigDecimal getMargin() {
        return margin;
    }

}
