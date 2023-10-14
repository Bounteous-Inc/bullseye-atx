package com.bullseyeaem.core.antiflicker.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.*;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SharedJavascriptSnippet {
    @ValueMapValue
    @Default(values = EMPTY)
    private String name;

    @ValueMapValue
    @Default(values = EMPTY)
    private String preFlickerCode;

    @ValueMapValue
    @Default(values = EMPTY)
    private String postFlickerCode;

    public String getName() {
        return name;
    }

    public String getPreFlickerCode() {
        return preFlickerCode;
    }

    public String getPostFlickerCode() {
        return postFlickerCode;
    }

    public String getFunctionCode() {
        return "\n" +
                "    function " + getPreFlickerFunctionName() + "(win, doc, targetFlickerConfig) { \n" +
                preFlickerCode + "\n" +
                "    }\n" +
                "\n" +
                "    function " + getPostFlickerFunctionName() + "(win, doc, targetFlickerConfig) { \n" +
                postFlickerCode + "\n" +
                "    }" ;
    }

    public String getPreFlickerFunctionName() {
        return generateJsFunctionName(this.name, "preFlicker");
    }

    public String getPostFlickerFunctionName() {
        return generateJsFunctionName(this.name, "postFlicker");
    }

    public static String generateJsFunctionName(final String text, final String prefix) {
        if (StringUtils.isNotBlank(text)) {
            return prefix + capitalize(
                    Arrays.stream(text.replaceAll("[(),.]", EMPTY).replaceAll("[-_]", SPACE).split(SPACE))
                    .map(StringUtils::capitalize)
                    .collect(joining()));
        }

        return EMPTY;
    }
}
