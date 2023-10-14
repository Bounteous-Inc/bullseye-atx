package com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs;

public class TargetEnvironment {
    private final long id;
    private final String name;
    private final boolean serveInactiveActivities;
    private final boolean isDefault;

    public TargetEnvironment(final long id, final String name, final boolean serveInactiveActivities, final boolean isDefault) {
        this.id = id;
        this.name = name;
        this.serveInactiveActivities = serveInactiveActivities;
        this.isDefault = isDefault;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isServeInactiveActivities() {
        return serveInactiveActivities;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
