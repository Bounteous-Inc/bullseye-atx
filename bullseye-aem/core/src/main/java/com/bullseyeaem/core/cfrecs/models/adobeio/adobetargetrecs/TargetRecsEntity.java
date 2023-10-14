package com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs;

import com.bullseyeaem.core.cfrecs.models.adobeio.AdobeIoObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.TARGET_CUSTOM_ATTR_CF_PATH;
import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.TARGET_CUSTOM_ATTR_IS_BULLSEYE;

public class TargetRecsEntity extends AdobeIoObject {

    private final String id;
    private final List<String> categories;
    private final TargetRecsEntityAttributes standardAttributes;
    private final Map<String,Object> customAttributes;
    private final boolean isBullseye;
    private final String cfPath;
    private int environment;

    public TargetRecsEntity(final String name, final String id, final List<String> categories,
                            final TargetRecsEntityAttributes standardAttributes, final Map<String,Object> customAttributes,
                            final boolean isBullseye, final String cfModelPath) {
        super(name);
        this.id = id;
        this.categories = ObjectUtils.clone(categories);
        this.standardAttributes = standardAttributes;
        this.customAttributes = customAttributes;
        this.isBullseye = isBullseye;
        this.cfPath = cfModelPath;
    }

    public String getId() {
        return id;
    }

    public List<String> getCategories() {
        return ObjectUtils.defaultIfNull(categories, List.of());
    }

    @JsonIgnore
    public TargetRecsEntityAttributes getStandardAttributes() {
        return standardAttributes;
    }

    @JsonIgnore
    public Map<String,Object> getCustomAttributes() {
        return customAttributes;
    }

    @JsonIgnore
    public boolean getIsBullseye() {
        return isBullseye;
    }

    @JsonIgnore
    public String getCfPath() {
        return cfPath;
    }

    public int getEnvironment() {
        return environment;
    }

    public void setEnvironment(int environment) {
        this.environment = environment;
    }

    public Map<String, Object> getAttributes() {
        final Map<String,Object> allAttributes = new HashMap<>();

        allAttributes.put(TARGET_CUSTOM_ATTR_IS_BULLSEYE, this.isBullseye);
        allAttributes.put(TARGET_CUSTOM_ATTR_CF_PATH, this.cfPath);

        allAttributes.put(TargetRecsEntityAttributes.MESSAGE, standardAttributes.getMessage());
        allAttributes.put(TargetRecsEntityAttributes.THUMBNAIL_URL, standardAttributes.getThumbnailUrl());
        allAttributes.put(TargetRecsEntityAttributes.VALUE, standardAttributes.getValue());
        allAttributes.put(TargetRecsEntityAttributes.PAGE_URL, standardAttributes.getPageUrl());
        allAttributes.put(TargetRecsEntityAttributes.INVENTORY, standardAttributes.getInventory());
        allAttributes.put(TargetRecsEntityAttributes.MARGIN, standardAttributes.getMargin());

        allAttributes.putAll(customAttributes);

        return allAttributes;
    }
}
