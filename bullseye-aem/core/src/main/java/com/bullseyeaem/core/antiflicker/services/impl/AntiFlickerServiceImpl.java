package com.bullseyeaem.core.antiflicker.services.impl;

import com.adobe.granite.ui.clientlibs.LibraryType;
import com.adobe.granite.ui.clientlibs.ProcessorProvider;
import com.adobe.granite.ui.clientlibs.script.ScriptProcessor;
import com.adobe.granite.ui.clientlibs.script.ScriptResource;
import com.adobe.granite.ui.clientlibs.script.StringScriptResource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.bullseyeaem.core.antiflicker.models.AntiFlickerConfig;
import com.bullseyeaem.core.antiflicker.models.SharedJavascriptSnippet;
import com.bullseyeaem.core.antiflicker.services.AntiFlickerService;
import com.bullseyeaem.core.common.exceptions.BullseyeException;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.util.StringBuilderWriter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.day.cq.commons.jcr.JcrConstants.*;
import static com.day.crx.JcrConstants.NT_UNSTRUCTURED;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.sling.api.resource.ResourceResolverFactory.SUBSERVICE;

@Component(
        service = AntiFlickerService.class
)
@Designate(ocd = AntiFlickerServiceImpl.Config.class)
public class AntiFlickerServiceImpl implements AntiFlickerService {
    private static final Logger LOG = LoggerFactory.getLogger(AntiFlickerServiceImpl.class);

    private static final String ANTI_FLICKER_SUBSERVICE = "anti-flicker";
    private static final String ECMASCRIPT_2016 = "ECMASCRIPT_2016";
    private static final String LANGUAGE_IN = "languageIn";
    private static final String LANGUAGE_OUT = "languageOut";
    private static final String ANTI_FLICKER_CONFIG_LIST = "antiFlickerConfigList";
    private static final String SHARED_JAVASCRIPT_SNIPPET_LIST = "sharedJavascriptSnippetList";
    private static final String STAGING_IDENTIFIER_LIST = "stagingIdentifierList";
    private static final String RESOURCE_TYPE_UTILITIES_CONFIG_ITEM = "bullseye-aem/utilities/anti-flicker/components/config-item";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    @Reference
    private ProcessorProvider processorProvider;

    private String configsRootPath;
    private String settingsRootPath;
    private String antiFlickerJsPath;

    @Override
    public String getJavaScript(final String stagingIdentifier, final boolean minify) {

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, ANTI_FLICKER_SUBSERVICE));

            final VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.setProperty(RuntimeConstants.SPACE_GOBBLING, RuntimeConstants.SpaceGobbling.LINES.toString());
            final VelocityContext velocityContext = new VelocityContext();

            final List<AntiFlickerConfig> antiFlickerConfigList = new ArrayList<>();
            final Resource antiFlickerConfigListResource = resourceResolver.getResource(configsRootPath);
            if (antiFlickerConfigListResource != null) {
                final Iterator<Resource> childIterable = antiFlickerConfigListResource.listChildren();
                while (childIterable.hasNext()) {
                    AntiFlickerConfig antiFlickerConfig = childIterable.next().adaptTo(AntiFlickerConfig.class);
                    if (antiFlickerConfig != null && antiFlickerConfig.isEnabled()) {
                        final String configStagingIdentifier = antiFlickerConfig.getStagingIdentifier();
                        if (isBlank(configStagingIdentifier) || stagingIdentifier.equalsIgnoreCase(configStagingIdentifier)) {
                            antiFlickerConfigList.add(antiFlickerConfig);
                        }
                    }
                }
            }

            velocityContext.put(ANTI_FLICKER_CONFIG_LIST, antiFlickerConfigList);

            final List<SharedJavascriptSnippet> sharedJavascriptSnippetList = getSharedJavascriptSnippetResourceList().stream().map(resource -> resource.adaptTo(SharedJavascriptSnippet.class)).collect(Collectors.toList());
            velocityContext.put(SHARED_JAVASCRIPT_SNIPPET_LIST, sharedJavascriptSnippetList);

            final Resource antiFlickerTemplateResource = resourceResolver.getResource(antiFlickerJsPath);
            if (antiFlickerTemplateResource != null) {
                final Resource childContent = antiFlickerTemplateResource.getChild(JCR_CONTENT);
                if (childContent != null) {
                    final StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
                    Velocity.evaluate(velocityContext, stringBuilderWriter, "anti-flicker", childContent.getValueMap().get(JCR_DATA, String.class));
                    String scriptSource = stringBuilderWriter.getBuilder().toString();

                    if (minify) {
                        final ScriptResource scriptResource = new StringScriptResource(scriptSource, "antiFlickerScript");
                        final ScriptProcessor scriptProcessor = processorProvider.getProcessor("gcc");

                        if (scriptProcessor != null) {
                            final StringWriter stringWriter = new StringWriter();
                            final Map<String, String> optionsMap = new HashMap<>();
                            optionsMap.put("default", "none");
                            optionsMap.put(LANGUAGE_IN, ECMASCRIPT_2016);
                            optionsMap.put(LANGUAGE_OUT, ECMASCRIPT_2016);
                            scriptProcessor.process(LibraryType.JS, scriptResource, stringWriter, optionsMap);
                            scriptSource = stringWriter.toString();
                        }
                    }
                    return scriptSource;
                }
            }
        } catch (LoginException e) {
            throw new BullseyeException("Unable to authenticate with service user.", "Unable to get anti-flicker script.", e);
        } catch (IOException e) {
            throw new BullseyeException("Unable to minify the anti-flicker JavaScript.", "Unable to get anti-flicker script.", e);
        }

        throw new BullseyeException("Unable to get anti-flicker script.", "Unable to get anti-flicker script.");
    }

    @Override
    public List<AntiFlickerConfig> getEnabledConfigs() {
        return getAllConfigs().stream().filter(AntiFlickerConfig::isEnabled).collect(Collectors.toList());
    }

    @Override
    public List<AntiFlickerConfig> getAllConfigs() {
        final List<AntiFlickerConfig> antiFlickerConfigList = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, ANTI_FLICKER_SUBSERVICE));

            Resource antiFlickerConfigListResource = resourceResolver.getResource(configsRootPath);
            if (antiFlickerConfigListResource != null) {
                final Iterator<Resource> childIterable = antiFlickerConfigListResource.listChildren();
                while (childIterable.hasNext()) {
                    final Resource childResource = childIterable.next();
                    if (childResource.isResourceType(NT_UNSTRUCTURED)) {
                        AntiFlickerConfig antiFlickerConfig = childResource.adaptTo(AntiFlickerConfig.class);
                        if (antiFlickerConfig != null) {
                            antiFlickerConfigList.add(antiFlickerConfig);
                        }
                    }
                }
            }
        } catch (LoginException e) {
            throw new BullseyeException("Unable to authenticate with service user.", "Unable to get anti-flicker configs.", e);
        }

        return antiFlickerConfigList;
    }

    @Override
    public void deleteConfigs(final String... configPaths) {
        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, "anti-flicker"));

            if (configPaths != null) {
                for (String configPath : configPaths) {
                    deleteConfig(configPath, resourceResolver);
                }
            }

            resourceResolver.commit();
        } catch (LoginException e) {
            throw new BullseyeException("Unable to authenticate.", "Unable to delete config.", e);
        } catch (PersistenceException e) {
            throw new BullseyeException("Unable to commit delete transaction.", "Unable to delete config.", e);
        }
    }

    @Override
    public List<AntiFlickerConfig> getConfigsForStagingIdentifier(final String stagingIdentifier) {
        return getEnabledConfigs().stream()
                .filter(config -> isBlank(config.getStagingIdentifier()) || stagingIdentifier.equalsIgnoreCase(config.getStagingIdentifier()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> getAllConfigSelectOptionsList() {
        return getAllConfigs().stream()
                .map(antiFlickerConfig -> {
                    return (Resource) new ValueMapResource(
                            antiFlickerConfig.getResource().getResourceResolver(),
                            antiFlickerConfig.getId(),
                            RESOURCE_TYPE_UTILITIES_CONFIG_ITEM,
                            antiFlickerConfig.getResource().getValueMap());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> getSharedJavascriptSnippetResourceList() {
        try {
            return getChildResourceList(settingsRootPath + "/" + SHARED_JAVASCRIPT_SNIPPET_LIST);
        } catch (LoginException e) {
            throw new BullseyeException("Unable to authenticate.", "Unable to get shared JavaScript snippets.", e);
        }
    }

    @Override
    public List<Resource> getSharedJavascriptSnippetSelectOptionsList() {
        return getSharedJavascriptSnippetResourceList().stream()
                .map(resource -> {
                    final ValueMap valueMap = resource.getValueMap();
                    return (Resource) new ValueMapResource(
                            resource.getResourceResolver(),
                            new ResourceMetadata(),
                            NT_UNSTRUCTURED,
                            new ValueMapDecorator(Map.of(
                                    "text", valueMap.get("name", EMPTY),
                                    "value", resource.getPath())
                            ));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> getStagingIdentifierResourceList() {
        try {
            return getChildResourceList(settingsRootPath + "/" + STAGING_IDENTIFIER_LIST);
        } catch (LoginException e) {
            throw new BullseyeException("Unable to authenticate.", "Unable to get staging identifiers.", e);
        }
    }

    @Override
    public List<Resource> getStagingIdentifierSelectOptionsList() {
        return getStagingIdentifierResourceList().stream()
                .map(resource -> {
                    final ValueMap valueMap = resource.getValueMap();
                    return (Resource) new ValueMapResource(
                            resource.getResourceResolver(),
                            new ResourceMetadata(),
                            NT_UNSTRUCTURED,
                            new ValueMapDecorator(Map.of(
                                    "text", String.format("%s (%s)", valueMap.get("name", EMPTY), valueMap.get("value", EMPTY)),
                                    "value", valueMap.get("value", EMPTY))
                            ));
                })
                .collect(Collectors.toList());
    }

    private void deleteConfig(final String configPath, final ResourceResolver resourceResolver) {
        try {
            final Resource configResource = resourceResolver.getResource(configPath);
            if (configResource != null) {
                resourceResolver.delete(configResource);
            }
        } catch (PersistenceException e) {
            throw new BullseyeException("Unable to delete config at " + configPath + ".", "Unable to delete anti-flicker config.", e);
        }
    }

    private List<Resource> getChildResourceList(final String parentResourcePath) throws LoginException {
        final List<Resource> resourceList = new ArrayList<>();

        final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, ANTI_FLICKER_SUBSERVICE));
        final Resource parentResource = resourceResolver.getResource(parentResourcePath);

        if (parentResource != null) {
            final Iterator<Resource> childIterable = parentResource.listChildren();
            while (childIterable.hasNext()) {
                resourceList.add(childIterable.next());
            }
        }

        return resourceList;
    }

    @Activate
    public void activate(Config config) {
        configsRootPath = config.configsRootPath();
        settingsRootPath = config.settingsRootPath();
        antiFlickerJsPath = config.antiFlickerJsPath();
    }

    @ObjectClassDefinition(name = "Bullseye AEM - Anti-Flicker Service",
            description = "Configuration for the Anti-Flicker feature of Bullseye AEM")
    @interface Config {
        @AttributeDefinition(
                name = "Configs Root Path",
                description = ""
        )
        String configsRootPath() default "/conf/global/settings/bullseye-aem/anti-flicker/configs";

        @AttributeDefinition(
                name = "Anti-Flicker Settings Root Path",
                description = ""
        )
        String settingsRootPath() default "/conf/global/settings/bullseye-aem/anti-flicker/settings";

        @AttributeDefinition(
                name = "Anti-Flicker JS Path",
                description = ""
        )
        String antiFlickerJsPath() default "/apps/bullseye-aem/utilities/anti-flicker/velocity/anti-flicker-js.vm";
    }
}
