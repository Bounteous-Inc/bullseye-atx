package com.bullseyeaem.core.cfrecs.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CfRecsCustomMapping {
    @ValueMapValue
    @Default(values = EMPTY)
    private String cfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String targetFieldName;

    public String getCfFieldName() {
        return cfFieldName;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }
}
