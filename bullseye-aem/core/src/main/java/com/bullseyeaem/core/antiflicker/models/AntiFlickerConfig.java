package com.bullseyeaem.core.antiflicker.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ResourcePath;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Named;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.*;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AntiFlickerConfig {

    @ValueMapValue
    @Default(values = EMPTY)
    private String name;

    @ValueMapValue
    @Default(values = EMPTY)
    private String pathRegex;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean checkHash;

    @ValueMapValue
    private List<String> domainList;

    @ValueMapValue
    private List<String> cssSelectorList;

    @ValueMapValue
    private String preFlickerFunction;

    @ValueMapValue
    private String postFlickerFunction;

    @ResourcePath
    @Named("sharedJavascriptPath")
    private Resource sharedJavascriptResource;

    @ValueMapValue
    private String stagingIdentifier;

    @ValueMapValue
    @Default(longValues = 3000)
    private Long timeout;

    @ValueMapValue
    private boolean enabled;

    @Self
    private Resource resource;

    public String getName() {
        return name;
    }

    public String getPathRegex() {
        return pathRegex;
    }

    public boolean isCheckHash() {
        return checkHash;
    }

    public String getPathRegexEscaped() {
        return "/" + strip(defaultIfBlank(pathRegex, ".*"), "/").replaceAll("(?<!\\\\)/", "/") + "/";
    }

    public List<String> getDomainList() {
        return emptyIfNull(domainList);
    }

    public List<String> getCssSelectorList() {
        return emptyIfNull(cssSelectorList);
    }

    public String getPreFlickerFunction() {
        return preFlickerFunction;
    }

    public String getPostFlickerFunction() {
        return postFlickerFunction;
    }

    public String getStagingIdentifier() {
        return stagingIdentifier;
    }

    public Long getTimeout() {
        return timeout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getId() {
        return this.resource.getPath();
    }

    public Resource getResource() {
        return this.resource;
    }

    public String getPreFlickerFunctionName() {
        //TODO: This probably will not work in publish
        if (sharedJavascriptResource != null) {
            final SharedJavascriptSnippet sharedJavascriptSnippet = sharedJavascriptResource.adaptTo(SharedJavascriptSnippet.class);
            return sharedJavascriptSnippet != null ? sharedJavascriptSnippet.getPreFlickerFunctionName() : SPACE;
        }
        return EMPTY;
    }

    public String getPostFlickerFunctionName() {
        //TODO: Lookup the name of function for the path
        if (sharedJavascriptResource != null) {
            final SharedJavascriptSnippet sharedJavascriptSnippet = sharedJavascriptResource.adaptTo(SharedJavascriptSnippet.class);
            return sharedJavascriptSnippet != null ? sharedJavascriptSnippet.getPostFlickerFunctionName() : SPACE;
        }
        return EMPTY;
    }

}
