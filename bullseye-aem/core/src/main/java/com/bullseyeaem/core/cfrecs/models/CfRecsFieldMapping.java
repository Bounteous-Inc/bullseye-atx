package com.bullseyeaem.core.cfrecs.models;

import com.adobe.cq.dam.cfm.FragmentTemplate;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CfRecsFieldMapping {
    @ValueMapValue
    @Default(values = EMPTY)
    private String cfModelPath;

    @ValueMapValue
    @Default(values = EMPTY)
    private String idCfFieldName;

    @ValueMapValue
    @Default
    private List<String> parentCfPaths;

    @ValueMapValue
    @Default(values = EMPTY)
    private String nameCfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String messageCfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String categoriesCfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String inventoryCfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String marginCfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String pageUrlCfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String thumbnailUrlCfFieldName;

    @ValueMapValue
    @Default(values = EMPTY)
    private String valueCfFieldName;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean enabled;

    @ChildResource(name = "custom-mappings")
    @Default()
    private List<CfRecsCustomMapping> customMappings;

    @SlingObject
    private Resource resource;


    public String getCfModelPath() {
        return cfModelPath;
    }

    public List<String> getParentCfPaths() {
        return emptyIfNull(parentCfPaths);
    }

    public String getIdCfFieldName() {
        return idCfFieldName;
    }

    public String getNameCfFieldName() {
        return nameCfFieldName;
    }

    public String getMessageCfFieldName() {
        return messageCfFieldName;
    }

    public String getCategoriesCfFieldName() {
        return categoriesCfFieldName;
    }

    public String getInventoryCfFieldName() {
        return inventoryCfFieldName;
    }

    public String getMarginCfFieldName() {
        return marginCfFieldName;
    }

    public String getPageUrlCfFieldName() {
        return pageUrlCfFieldName;
    }

    public String getThumbnailUrlCfFieldName() {
        return thumbnailUrlCfFieldName;
    }

    public String getValueCfFieldName() {
        return valueCfFieldName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<CfRecsCustomMapping> getCustomMappings() {
        return emptyIfNull(customMappings);
    }

    @SuppressWarnings("unused")
    public String getCfModelName() {
        final Resource cfModelResource = resource.getResourceResolver().getResource(cfModelPath);
        if (cfModelResource != null) {
            final FragmentTemplate fragmentTemplate = cfModelResource.adaptTo(FragmentTemplate.class);
            return (fragmentTemplate != null) ? fragmentTemplate.getTitle() : EMPTY;
        }

        return EMPTY;
    }

    @SuppressWarnings("unused")
    public String getMappingsText() {
        //TODO: Show field labels instead of names
        return
                "<table>" +
                        "<tr><td>id</td><td>: " + idCfFieldName + "</td></tr>" +
                        "<tr><td>name</td><td>: " + nameCfFieldName + "</td></tr>" +
                        "<tr><td>message</td><td>: " + messageCfFieldName + "</td></tr>" +
                        "<tr><td>categories</td><td>: " + categoriesCfFieldName + "</td></tr>" +
                        "<tr><td>inventory</td><td>: " + inventoryCfFieldName + "</td></tr>" +
                        "<tr><td>margin</td><td>: " + marginCfFieldName + "</td></tr>" +
                        "<tr><td>pageUrl</td><td>: " + pageUrlCfFieldName + "</td></tr>" +
                        "<tr><td>thumbnailUrl</td><td>: " + thumbnailUrlCfFieldName + "</td></tr>" +
                        "<tr><td>value</td><td>: " + valueCfFieldName + "</td></tr>" +

                        emptyIfNull(customMappings).stream().map(customMapping -> "<tr><td>" + customMapping.getTargetFieldName() + "</td><td>: " + customMapping.getCfFieldName() + "</td></tr>").collect(Collectors.joining()) +
                "</table>";
    }

    public String getPath() {
        return resource.getPath();
    }
}
