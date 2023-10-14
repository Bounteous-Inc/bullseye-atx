package com.bullseyeaem.core.cfrecs.services.impl;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.DataType;
import com.adobe.cq.dam.cfm.ElementTemplate;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.bullseyeaem.core.cfrecs.models.CfRecsCustomMapping;
import com.bullseyeaem.core.cfrecs.models.CfRecsFieldMapping;
import com.bullseyeaem.core.cfrecs.models.StructuredContentFragment;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.TargetRecsEntity;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.TargetRecsEntityAttributes;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.enums.TagPartEnum;
import com.bullseyeaem.core.cfrecs.predicates.ContentFragmentIsOfModelTypePredicate;
import com.bullseyeaem.core.cfrecs.services.CfRecsMappingsService;
import com.bullseyeaem.core.cfrecs.services.TargetApiService;
import com.bullseyeaem.core.common.exceptions.BullseyeException;
import com.bullseyeaem.core.common.util.RepositoryUtil;
import com.day.cq.tagging.TagManager;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bullseyeaem.core.cfrecs.jobs.CfRecsJobExecutor.CFRECS_PUSH_CF_MAPPINGS_TOPIC;
import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.CF_RECS_SUBSERVICE;
import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.SEMANTIC_TYPE_TAG;
import static com.bullseyeaem.core.common.util.RepositoryUtil.findAllFilteredResources;
import static com.bullseyeaem.core.common.util.RepositoryUtil.findFirstFilteredResources;
import static com.day.crx.JcrConstants.NT_UNSTRUCTURED;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.sling.api.resource.ResourceResolverFactory.SUBSERVICE;

@Component(
        service = CfRecsMappingsService.class
)
public class CfRecsMappingsServiceImpl implements CfRecsMappingsService {

    private static final Logger LOG = LoggerFactory.getLogger(CfRecsMappingsService.class);
    private static final String DAM_ROOT_PATH = "/content/dam";
    static final String FIELD_MAPPINGS_RELATIVE_PATH = "settings/bullseye-aem/cf-recs/field-mappings";
    static final String RESOURCE_TYPE_MAPPING_TABLE_ITEM = "bullseye-aem/utilities/cf-recs/features/field-mappings/components/mapping-table-item";
    static final String[] STANDARD_MAPPING_VARIABLES = {
            "idCfFieldName",
            "nameCfFieldName",
            "messageCfFieldName",
            "categoriesCfFieldName",
            "brandCfFieldName",
            "inventoryCfFieldName",
            "marginCfFieldName",
            "pageUrlCfFieldName",
            "thumbnailUrlCfFieldName",
            "valueCfFieldName"
    };
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private JobManager jobManager;

    @Reference
    private TargetApiService targetApiService;

    @Override
    public List<Resource> getMappingsTableItemsResourceList(final Resource confFolderResource) {
        final List<Resource> options = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            if (confFolderResource != null) {
                final Resource fieldMappingsParentResource = resourceResolver
                        .getResource(confFolderResource.getPath() + "/" + FIELD_MAPPINGS_RELATIVE_PATH);

                if (fieldMappingsParentResource != null) {
                    final Iterator<Resource> mappingResourceList = fieldMappingsParentResource.listChildren();
                    while (mappingResourceList.hasNext()) {
                        final Resource mappingResource = mappingResourceList.next();

                        final ValueMapResource valueMapResource = new ValueMapResource(
                                resourceResolver,
                                mappingResource.getPath(),
                                RESOURCE_TYPE_MAPPING_TABLE_ITEM,
                                mappingResource.getValueMap());

                        options.add(valueMapResource);
                    }
                }
            }
        } catch (LoginException e) {
            LOG.error("Error retrieving CF Recs mapping resource list.");
        }

        return options;
    }


    @Override
    public List<Resource> getCfModelSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath) {
        final List<Resource> options = new ArrayList<>();

        if (confFolderResource != null) {
            final Resource modelsFolderResource = confFolderResource.getChild("settings/dam/cfm/models");

            if (modelsFolderResource != null) {
                Iterator<Resource> childIterator = modelsFolderResource.listChildren();
                while (childIterator.hasNext()) {
                    final Resource cfModelResource = childIterator.next();
                    final ValueMapResource cfModelValueMapResource = getCfModelValueMapResource(cfModelResource, cfModelPath);

                    if (cfModelValueMapResource != null) {
                        options.add(cfModelValueMapResource);
                    }
                }
            }
        }

        return options;
    }

    @Override
    public List<Resource> getMappedCfModelSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath) {
        try {
            List<String> mappedCfModelPaths = getCfModelPathsFromAllMappings(confFolderResource);

            return getCfModelSelectOptionsResourceList(confFolderResource, cfModelPath).stream()
                    .filter(resource -> mappedCfModelPaths.contains(resource.getValueMap().get("value", EMPTY)))
                    .collect(Collectors.toList());
        } catch (LoginException e) {
            LOG.error("Error retrieving Mapped CF models resource list.");
            throw new BullseyeException("Error retrieving Mapped CF models resource list.", e);
        }
    }

    @Override
    public List<Resource> getUnmappedCfModelSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath) {
        try {
            List<String> mappedCfModelPaths = getCfModelPathsFromAllMappings(confFolderResource);

            return getCfModelSelectOptionsResourceList(confFolderResource, cfModelPath).stream()
                    .filter(resource -> !mappedCfModelPaths.contains(resource.getValueMap().get("value", EMPTY)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Error retrieving Unmapped CF models resource list.");
            throw new BullseyeException("Error retrieving Unmapped CF models resource list.", e);
        }
    }

    @Override
    public List<Resource> getFieldMappingSelectOptionsResourceList(final Resource confFolderResource) {
        final List<Resource> options = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            if (confFolderResource != null) {
                final Resource fieldMappingsParentResource = resourceResolver
                        .getResource(confFolderResource.getPath() + "/" + FIELD_MAPPINGS_RELATIVE_PATH);

                if (fieldMappingsParentResource != null) {
                    final Iterator<Resource> mappingResourceList = fieldMappingsParentResource.listChildren();
                    while (mappingResourceList.hasNext()) {
                        final Resource mappingResource = mappingResourceList.next();

                        final String cfModelPath = mappingResource.getValueMap().get("cfModelPath", String.class);
                        if (StringUtils.isNotBlank(cfModelPath)) {
                            final Resource cfModelResource = resourceResolver.getResource(cfModelPath);
                            if (cfModelResource != null) {
                                final FragmentTemplate cfModelFragmentTemplate = cfModelResource.adaptTo(FragmentTemplate.class);

                                if (cfModelFragmentTemplate != null) {
                                    options.add(new ValueMapResource(
                                            resourceResolver,
                                            new ResourceMetadata(),
                                            NT_UNSTRUCTURED,
                                            new ValueMapDecorator(Map.of(
                                                    "text", cfModelFragmentTemplate.getTitle(),
                                                    "value", mappingResource.getPath()
                                            ))
                                    ));
                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error retrieving CF Recs mapping resource list.");
        }

        return options;
    }

    @Override
    public List<Resource> getCfModelFieldSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath) {
        final List<Resource> options = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));
            final Resource selectedCfModelResource = resourceResolver.getResource(cfModelPath);

            if (selectedCfModelResource != null) {
                final FragmentTemplate cfModelFragmentTemplate = selectedCfModelResource.adaptTo(FragmentTemplate.class);
                if (cfModelFragmentTemplate != null) {
                    final Iterator<ElementTemplate> fieldElements = cfModelFragmentTemplate.getElements();
                    while (fieldElements.hasNext()) {
                        final ElementTemplate fieldElement = fieldElements.next();

                        if ("tag".equals(fieldElement.getDataType().getSemanticType())) {
                            options.add(new ValueMapResource(
                                    selectedCfModelResource.getResourceResolver(),
                                    new ResourceMetadata(),
                                    NT_UNSTRUCTURED,
                                    new ValueMapDecorator(Map.of(
                                            "text", fieldElement.getTitle() + " (Tag Title)",
                                            "value", fieldElement.getName() + ".title"
                                    ))
                            ));

                            options.add(new ValueMapResource(
                                    selectedCfModelResource.getResourceResolver(),
                                    new ResourceMetadata(),
                                    NT_UNSTRUCTURED,
                                    new ValueMapDecorator(Map.of(
                                            "text", fieldElement.getTitle() + " (Tag Name)",
                                            "value", fieldElement.getName() + ".name"
                                    ))
                            ));
                        } else {
                            options.add(new ValueMapResource(
                                    selectedCfModelResource.getResourceResolver(),
                                    new ResourceMetadata(),
                                    NT_UNSTRUCTURED,
                                    new ValueMapDecorator(Map.of(
                                            "text", fieldElement.getTitle(),
                                            "value", fieldElement.getName()
                                    ))
                            ));
                        }
                    }

                    final StructuredContentFragment cfSlingModel = getCfSlingModel(resourceResolver, selectedCfModelResource);
                    if (cfSlingModel != null) {

                        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(cfSlingModel.getClass(), Object.class).getPropertyDescriptors();
                        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                            options.add(new ValueMapResource(
                                    selectedCfModelResource.getResourceResolver(),
                                    new ResourceMetadata(),
                                    NT_UNSTRUCTURED,
                                    new ValueMapDecorator(Map.of(
                                            "text", propertyDescriptor.getDisplayName() + " (of " + cfSlingModel.getClass().getSimpleName() + " Sling Model)",
                                            "value", "model." + cfSlingModel.getClass().getName().replace('.', '-') + "." + propertyDescriptor.getName()
                                    ))
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error retrieving CF model fields resource list.");
        }

        return options;
    }

    @Override
    public List<String> getMappedTargetFieldNameList(final Resource confFolderResource) {
        //TODO: This method is currently unused.
        final Set<String> fieldNameSet = new LinkedHashSet<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            if (confFolderResource != null) {
                final Resource fieldMappingsParentResource = resourceResolver
                        .getResource(confFolderResource.getPath() + "/" + FIELD_MAPPINGS_RELATIVE_PATH);

                if (fieldMappingsParentResource != null) {
                    final Iterator<Resource> mappingResourceList = fieldMappingsParentResource.listChildren();
                    while (mappingResourceList.hasNext()) {
                        final Resource mappingResource = mappingResourceList.next();

                        final ValueMap mappingValueMap = mappingResource.getValueMap();

                        for (String mappingVariable : STANDARD_MAPPING_VARIABLES) {
                            if (mappingValueMap.containsKey(mappingVariable)) {
                                fieldNameSet.add(mappingValueMap.get(mappingVariable, String.class));
                            }
                        }
                        //TODO: Add custom mappings
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error retrieving CF Recs mapping field name list.");
        }

        return new ArrayList<>(fieldNameSet);
    }

    @Override
    public Set<String> getMappedTargetFieldNameList(final Resource confFolderResource, final String cfModelPath) {
        //TODO: This method is currently unused.
        return Set.of("id", "name", "message");
    }

    @Override
    public Job startJob() {
        final Map<String, Object> props = new HashMap<>();
        props.put("item1", "/something");

        return jobManager.addJob(CFRECS_PUSH_CF_MAPPINGS_TOPIC, props);
    }

    @Override
    public void pushMappingsToTarget(final String mappingPath) {
        if (isNotBlank(mappingPath)) {
            try {
                final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

                final TagManager tagManager = resourceResolver.adaptTo(TagManager.class);

                final CfRecsFieldMapping cfRecsFieldMapping = Optional.of(mappingPath)
                        .map(resourceResolver::getResource)
                        .filter(Objects::nonNull)
                        .map(mappingResource -> mappingResource.adaptTo(CfRecsFieldMapping.class))
                        .filter(Objects::nonNull)
                        .filter(CfRecsFieldMapping::isEnabled)
                        .orElse(null);

                if (cfRecsFieldMapping != null) {
                    JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
                    JsonArrayBuilder entitiesArrayBuilder = Json.createArrayBuilder();
                    jsonObjectBuilder.add("entities", entitiesArrayBuilder);

                    final List<TargetRecsEntity> entityList = new ArrayList<>();

                    final List<String> parentCfPaths = cfRecsFieldMapping.getParentCfPaths();

                    for (String parentCfPath : parentCfPaths) {
                        final Resource parentCfResource = resourceResolver.getResource(parentCfPath);
                        if (parentCfResource != null) {
                            final List<Resource> contentFragmentResourceList = findAllFilteredResources(parentCfResource,
                                    new ContentFragmentIsOfModelTypePredicate(cfRecsFieldMapping.getCfModelPath()));

                            for (final Resource contentFragmentResource : contentFragmentResourceList) {
                                ContentFragment contentFragment = contentFragmentResource.adaptTo(ContentFragment.class);
                                if (contentFragment != null) {
                                    entityList.add(convertToTargetRecsEntity(contentFragment, cfRecsFieldMapping, tagManager));
                                }
                            }
                        }
                    }

                    //TODO: Chop this list up to some specified max sizes to not overload the Target API
                    if (entityList.size() > 0) {
                        targetApiService.pushTargetRecsEntities(entityList, null);
                    }
                }
            } catch (LoginException e) {
                throw new BullseyeException(
                        "Error logging in with the service user.",
                        "There was a problem pushing the Content Fragment to Target.",
                        e);
            }
        }
    }

    @Override
    public void pushContentFragmentsToTarget(List<String> cfPaths) {
        if (cfPaths == null) {
            return;
        }

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));
            final TagManager tagManager = resourceResolver.adaptTo(TagManager.class);

            final List<TargetRecsEntity> entityList = new ArrayList<>();
            for (String cfPath : cfPaths) {
                final Resource cfResource = resourceResolver.getResource(cfPath);
                if (cfResource != null) {
                    final CfRecsFieldMapping cfRecsFieldMapping = findFieldMappingForContentFragment(cfResource, resourceResolver);
                    if (cfRecsFieldMapping != null) {
                        ContentFragment contentFragment = cfResource.adaptTo(ContentFragment.class);
                        if (contentFragment != null) {
                            entityList.add(convertToTargetRecsEntity(contentFragment, cfRecsFieldMapping, tagManager));
                        }
                    }
                }
            }

            if (entityList.size() > 0) {
                targetApiService.pushTargetRecsEntities(entityList, null);
            }

        } catch (LoginException e) {
            throw new BullseyeException(
                    "Error logging in with the service user.",
                    "There was a problem pushing the Content Fragments to Target.",
                    e);
        }
    }

    @Override
    public void deleteMappings(String... mappingPaths) {
        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, "anti-flicker"));

            if (mappingPaths != null) {
                for (String mappingPath : mappingPaths) {
                    deleteMapping(mappingPath, resourceResolver);
                }
            }

            resourceResolver.commit();
        } catch (LoginException e) {
            throw new BullseyeException("Unable to authenticate.", e);
        } catch (PersistenceException e) {
            throw new BullseyeException("Unable to commit delete transaction.", e);
        }
    }

    @Override
    public String getEntityIdForContentFragmentResource(final Resource cfResource) {
        if (cfResource == null) {
            return EMPTY;
        }

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));
            final TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
            final ContentFragment contentFragment = cfResource.adaptTo(ContentFragment.class);
            final CfRecsFieldMapping cfRecsFieldMapping = findFieldMappingForContentFragment(cfResource, resourceResolver);

            return getValueAs(contentFragment, cfRecsFieldMapping.getIdCfFieldName(), String.class, tagManager);
        } catch (LoginException e) {
            throw new BullseyeException("Unable to authenticate.", e);
        }
    }

    private void deleteMapping(String mappingPath, ResourceResolver resourceResolver) {
        try {
            final Resource mappingResource = resourceResolver.getResource(mappingPath);
            if (mappingResource != null) {
                resourceResolver.delete(mappingResource);
            }
        } catch (PersistenceException e) {
            throw new BullseyeException("Unable to delete mapping at " + mappingPath + ".", e);
        }
    }

    private TargetRecsEntity convertToTargetRecsEntity(final ContentFragment contentFragment, final CfRecsFieldMapping cfRecsFieldMapping, final TagManager tagManager) {
        final Resource cfResource = contentFragment.adaptTo(Resource.class);
        if (cfResource == null) {
            return null;  // This should never happen
        }

        Map<String,Object> customAttributes = new LinkedHashMap<>();
        for (CfRecsCustomMapping cfRecsCustomMapping : cfRecsFieldMapping.getCustomMappings()) {
            customAttributes.put(
                    cfRecsCustomMapping.getTargetFieldName(),
                    //TODO: Handle other types besides String based on the type of field in the Content Fragment
                    getValueAs(contentFragment, cfRecsCustomMapping.getCfFieldName(), String.class, tagManager));
        }

        TargetRecsEntityAttributes targetRecsEntityAttributes = new TargetRecsEntityAttributes(
                getValueAs(contentFragment, cfRecsFieldMapping.getMessageCfFieldName(), String.class, tagManager),
                getValueAs(contentFragment, cfRecsFieldMapping.getThumbnailUrlCfFieldName(), String.class, tagManager),
                getValueAs(contentFragment, cfRecsFieldMapping.getValueCfFieldName(), BigDecimal.class, tagManager),
                //TODO: Allow an externalizer to be specified
                getValueAs(contentFragment, cfRecsFieldMapping.getPageUrlCfFieldName(), String.class, tagManager),
                getValueAs(contentFragment, cfRecsFieldMapping.getInventoryCfFieldName(), Integer.class, tagManager),
                getValueAs(contentFragment, cfRecsFieldMapping.getMarginCfFieldName(), BigDecimal.class, tagManager));

        return new TargetRecsEntity(
                getValueAs(contentFragment, cfRecsFieldMapping.getNameCfFieldName(), String.class, tagManager),
                getValueAs(contentFragment, cfRecsFieldMapping.getIdCfFieldName(), String.class, tagManager),
                //TODO: See if this can be cleaned up
                Arrays.asList(defaultIfNull(getValueAs(contentFragment, cfRecsFieldMapping.getCategoriesCfFieldName(), String[].class, tagManager), new String[0])),
                targetRecsEntityAttributes,
                customAttributes,
                true,
                cfResource.getPath()
        );
    }

    private List<String> getCfModelPathsFromAllMappings(final Resource confFolderResource) throws LoginException {
        List<String> cfModelPathsList = new ArrayList<>();

        final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

        if (confFolderResource != null) {
            final Resource fieldMappingsParentResource = resourceResolver
                    .getResource(confFolderResource.getPath() + "/" + FIELD_MAPPINGS_RELATIVE_PATH);

            if (fieldMappingsParentResource != null) {
                cfModelPathsList = Lists.newArrayList(fieldMappingsParentResource.listChildren()).stream()
                        .map(Resource::getValueMap)
                        .map(valueMap -> valueMap.get("cfModelPath", String.class))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
            }
        }

        return cfModelPathsList;
    }

    private StructuredContentFragment getCfSlingModel(ResourceResolver resourceResolver, Resource selectedCfModelResource) {
        /*
        SyntheticResource syntheticResource = new SyntheticResource(resourceResolver, "/content/dam/synthetic-resource", "dam:Asset");
        syntheticResource.getValueMap().put("jcr:title", "Synthetic Resource");
        Resource contentResource = resourceResolver.create(syntheticResource, "jcr:content", Map.of("jcr:primaryType", "dam:AssetContent"));
        Resource dataResource = resourceResolver.create(contentResource, "data",
                Map.of("cq:model", selectedCfModelResource.getPath(),
                        JcrConstants.JCR_PRIMARYTYPE, NT_UNSTRUCTURED));

        StructuredContentFragment structuredContentFragment = modelFactory.createModel(syntheticResource, StructuredContentFragment.class);
        */

        // NOTE: Using a SyntheticResource did not work so there needs to be a content fragment in the DAM of the given CFM
        final Resource cfResource = findFirstFilteredResources(resourceResolver.getResource(DAM_ROOT_PATH),
                new ContentFragmentIsOfModelTypePredicate(selectedCfModelResource.getPath()));

        if (cfResource != null) {
            return modelFactory.createModel(cfResource, StructuredContentFragment.class);
        }

        return null;
    }

    private ValueMapResource getCfModelValueMapResource(final Resource cfModelResource, final String selectedCfModelPathValue) {
        if (cfModelResource != null) {
            final FragmentTemplate fragmentTemplate = cfModelResource.adaptTo(FragmentTemplate.class);

            if (fragmentTemplate != null) {

                final ValueMap newValueMap = new ValueMapDecorator(Map.of(
                        "text", fragmentTemplate.getTitle(),
                        "value", cfModelResource.getPath(),
                        "selected", StringUtils.equals(selectedCfModelPathValue, cfModelResource.getPath())
                ));

                return new ValueMapResource(
                        cfModelResource.getResourceResolver(),
                        new ResourceMetadata(),
                        NT_UNSTRUCTURED,
                        newValueMap);
            }
        }

        return null;
    }

    private <T> T getValueAs(final ContentFragment contentFragment, final String cfFieldName, final Class<T> type, final TagManager tagManager) {
        if (isBlank(cfFieldName)) {
            return null;
        }

        final String[] cfFieldNameParts = split(cfFieldName, ".");

        if (cfFieldNameParts[0].equals("model")) {
            if (cfFieldNameParts.length == 3) {
                return getSlingModelValue(contentFragment, type, cfFieldNameParts[1], cfFieldNameParts[2]);
            }
        } else {
            final ContentElement contentElement = getContentElement(contentFragment, cfFieldName);

            final FragmentData fragmentData = contentElement.getValue();
            final DataType dataType = fragmentData.getDataType();
            final String valueType = dataType.getValueType();
            final String semanticType = dataType.getSemanticType();

            //TODO: Support other special types
            if (SEMANTIC_TYPE_TAG.equals(semanticType)) {
                if (dataType.isMultiValue()) {
                    final String[] valueArray = fragmentData.getValue(String[].class);
                    if (valueArray != null) {
                        return type.cast(Arrays.stream(valueArray).map(value -> getTagPartValue(value, cfFieldName, tagManager)).toArray(String[]::new));
                    }
                } else {
                    return type.cast(getTagPartValue(fragmentData.getValue(String.class), cfFieldName, tagManager));
                }
            }

            return fragmentData.getValue(type);
        }

        return null;
    }

    private String getTagPartValue(final String tagId, final String tagPart, final TagManager tagManager) {
        if (TagPartEnum.NAME.getPartName().equals(tagPart)) {
            return tagManager.resolve(tagId).getName();
        } else if (TagPartEnum.TITLE.getPartName().equals(tagPart)) {
            return tagManager.resolve(tagId).getTitle();
        }

        return tagId;
    }

    private <T> T getSlingModelValue(final ContentFragment contentFragment, final Class<T> type, final String slingModelName, final String accessorName) {
        try {
            final Class<?> slingModelClass = Class.forName(slingModelName.replace('-', '.'));
            final Resource cfResource = contentFragment.adaptTo(Resource.class);
            if (cfResource != null) {
                Object slingModel = modelFactory.createModel(cfResource, slingModelClass);
                BeanInfo slingModelBeanInfo = Introspector.getBeanInfo(slingModelClass, Object.class);
                PropertyDescriptor propertyDescriptor = Arrays.stream(slingModelBeanInfo.getPropertyDescriptors()).filter(descriptor -> descriptor.getName().equals(accessorName)).findFirst().orElse(null);

                if (propertyDescriptor != null) {
                    Object valueObject = propertyDescriptor.getReadMethod().invoke(slingModel);
                    //TODO: Handle various return types
                    return type.cast(valueObject);
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to load Sling model for " + accessorName);
        }

        return null;
    }

    private ContentElement getContentElement(final ContentFragment contentFragment, final String cfFieldName) {
        return contentFragment.getElement(split(cfFieldName, ".")[0]);
    }

    public CfRecsFieldMapping findFieldMappingForContentFragment(final Resource cfResource, final ResourceResolver resourceResolver) {
        if (cfResource == null) {
            return null;
        }

        Resource cfmResource = Optional.of(cfResource)
                .map(resource -> resource.adaptTo(ContentFragment.class))
                .filter(Objects::nonNull)
                .map(ContentFragment::getTemplate)
                .map(template -> template.adaptTo(Resource.class))
                .orElse(null);
        if (cfmResource != null) {

            Resource confFolderResource = RepositoryUtil.getConfFolderResource(cfmResource);

            if (confFolderResource != null) {
                final Resource fieldMappingsParentResource = resourceResolver
                        .getResource(confFolderResource.getPath() + "/" + FIELD_MAPPINGS_RELATIVE_PATH);

                if (fieldMappingsParentResource != null) {
                    return Lists.newArrayList(fieldMappingsParentResource.listChildren()).stream()
                            .map(resource -> resource.adaptTo(CfRecsFieldMapping.class))
                            .filter(Objects::nonNull)
                            .filter(mappingModel -> {
                                return cfmResource.getPath().equals(mappingModel.getCfModelPath() + "/jcr:content");
                            })
                            .filter(mappingModel -> {
                                return mappingModel.getParentCfPaths().stream().anyMatch(parentpath -> cfResource.getPath().startsWith(parentpath));
                            })
                            .findFirst()
                            .orElse(null);
                }
            }
        }

        return null;
    }

}
