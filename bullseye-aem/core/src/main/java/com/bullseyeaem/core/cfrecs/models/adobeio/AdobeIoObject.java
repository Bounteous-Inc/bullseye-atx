package com.bullseyeaem.core.cfrecs.models.adobeio;

public abstract class AdobeIoObject {
    protected String name;

    protected AdobeIoObject(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
