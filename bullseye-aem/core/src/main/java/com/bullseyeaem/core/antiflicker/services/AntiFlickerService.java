package com.bullseyeaem.core.antiflicker.services;

import com.bullseyeaem.core.antiflicker.models.AntiFlickerConfig;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface AntiFlickerService {
    /**
     * Get JavaScript code based on the anti-flicker configurations.
     * @param stagingIdentifier an optional staging identifier to filter included configurations
     * @param minify if true, minify the resulting JavaScript
     * @return valid JavaScript code for anti-flicker.
     */
    String getJavaScript(final String stagingIdentifier, boolean minify);

    /**
     * Get a list of all anti-flicker configurations, regardless if they're enabled or not.
     * @return a list of AntiFlickerConfig Sling Models.
     */
    List<AntiFlickerConfig> getAllConfigs();

    /**
     * Get a list of enabled anti-flicker configurations only.
     * @return a list of AntiFlickerConfig Sling Models.
     */
    List<AntiFlickerConfig> getEnabledConfigs();

    void deleteConfigs(String... configPaths);

    List<AntiFlickerConfig> getConfigsForStagingIdentifier(final String stagingIdentifier);

    List<Resource> getAllConfigSelectOptionsList();

    List<Resource> getSharedJavascriptSnippetResourceList();

    List<Resource> getSharedJavascriptSnippetSelectOptionsList();

    List<Resource> getStagingIdentifierResourceList();

    List<Resource> getStagingIdentifierSelectOptionsList();
}
