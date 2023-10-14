package com.bullseyeaem.core.common.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class FeaturePageRow {
    @ValueMapValue
    @Default(values = EMPTY)
    private String name;

    @ValueMapValue
    @Default(values = EMPTY)
    private String href;

    @ValueMapValue
    @Default(values = "graphPathing")
    private String icon;

    @SlingObject
    private Resource resource;

    public String getName() {
        return name;
    }

    public String getPath() {
        return resource.getPath();
    }

    public String getHref() {
        return href;
    }

    public String getIcon() {
        return icon;
    }
}
