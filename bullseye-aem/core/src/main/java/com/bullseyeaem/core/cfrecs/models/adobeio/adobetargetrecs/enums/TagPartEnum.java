package com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.enums;

public enum TagPartEnum {
    NAME("name"),
    TITLE("name");

    private final String partName;

    TagPartEnum(String partName) {
        this.partName = partName;
    }

    public String getPartName() {
        return partName;
    }
}
