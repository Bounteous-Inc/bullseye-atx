package com.bullseyeaem.core.antiflicker.models;

import com.bullseyeaem.core.antiflicker.services.AntiFlickerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;

/**
 * Use this Sling Model to embed the anti-flicker code in a page, as opposed to calling it remotely.
 * For example:
 * <pre>{@code
 *     <script data-sly-use.script="${'com.bullseyeaem.core.antiflicker.models.AntiFlickerConfigScript' @minify=false}">
 *         ${script.javaScript @context='unsafe'}
 *     </script>
 * @code}</pre>
 *
 *
 */
@Model(adaptables = {SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AntiFlickerConfigScript {

    @OSGiService
    private AntiFlickerService antiFlickerService;

    @RequestAttribute
    @Default(booleanValues = true)
    private boolean minify;

    @RequestAttribute
    @Default(values = StringUtils.EMPTY)
    private String stagingIdentifier;

    public String getJavaScript() {
        return antiFlickerService.getJavaScript(stagingIdentifier, minify);
    }

    public boolean getMinify() {
        return minify;
    }

    public String getStagingIdentifier() {
        return stagingIdentifier;
    }
}

