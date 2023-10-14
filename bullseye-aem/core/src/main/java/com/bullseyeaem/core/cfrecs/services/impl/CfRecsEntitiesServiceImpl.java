package com.bullseyeaem.core.cfrecs.services.impl;

import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch.TargetRecsCatalogSearchQuery;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch.TargetRecsCatalogSearchResult;
import com.bullseyeaem.core.cfrecs.services.CfRecsEntitiesService;
import com.bullseyeaem.core.cfrecs.services.TargetApiService;
import com.bullseyeaem.core.common.exceptions.BullseyeException;
import com.bullseyeaem.core.common.util.BullseyeConstants;
import com.bullseyeaem.core.common.util.DataSourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.CF_RECS_SUBSERVICE;
import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.STANDARD_TARGET_FIELD_NAMES;
import static com.bullseyeaem.core.common.util.BullseyeConstants.FIXED_WIDTH;
import static com.bullseyeaem.core.common.util.BullseyeConstants.SORTABLE;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.sling.api.resource.ResourceResolverFactory.SUBSERVICE;

@Component(
        service = CfRecsEntitiesService.class
)
public class CfRecsEntitiesServiceImpl implements CfRecsEntitiesService {
    private static final Logger LOG = LoggerFactory.getLogger(CfRecsEntitiesServiceImpl.class);

    private static final String RESOURCE_TYPE_ENTITIES_TABLE_ITEM = "bullseye-aem/utilities/cf-recs/features/entities-list/components/entities-table-item";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private TargetApiService targetApiService;

    @Override
    public List<Resource> getEntitiesTableItemsResourceList(final Resource confFolderResource) {
        List<Resource> entitiesResourceList = new ArrayList<>();

        if (confFolderResource == null) {
            return entitiesResourceList;
        }

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            TargetRecsCatalogSearchQuery targetRecsCatalogSearchQuery = new TargetRecsCatalogSearchQuery(16742, List.of(STANDARD_TARGET_FIELD_NAMES));
            TargetRecsCatalogSearchResult targetRecsCatalogSearchResult = targetApiService.searchTargetRecsEntities(targetRecsCatalogSearchQuery);

            entitiesResourceList = targetRecsCatalogSearchResult.getHits().stream()
                    .map(hit -> {
                        final ValueMap valueMapDecorator = new ValueMapDecorator(new LinkedHashMap<>());
                        for (String targetFieldName : STANDARD_TARGET_FIELD_NAMES) {
                            valueMapDecorator.put(targetFieldName, hit.get(targetFieldName));
                        }

                        // Create item Resource with a type of the component that renders the row.
                        // NOTE: It doesn't really matter what the path is.
                        return new ValueMapResource(
                                resourceResolver,
                                hit.get("id").toString(),
                                RESOURCE_TYPE_ENTITIES_TABLE_ITEM,
                                valueMapDecorator);

                    }).collect(Collectors.toList());
        } catch (LoginException e) {
            throw new BullseyeException(
                    "Error logging in with the service user.",
                    "There was a problem fetching the entities from Target.",
                    e);
        }

        return entitiesResourceList;
    }

    @Override
    public List<Resource> getEntitiesTableColumnsResourceList() {
        final List<Map<String, Object>> fieldList = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            // First add the select column
            fieldList.add(Map.of(
                    JCR_TITLE, EMPTY,
                    FIXED_WIDTH, true));

            // Next add columns for all the standard Target fields and make them sortable
            // NOTE: Do not try to add custom attributes because the catalog search API will fail if any don't match what's
            // in Target, which will be the case if an upload has not yet occurred.
            fieldList.addAll(Stream.of(STANDARD_TARGET_FIELD_NAMES)
                    .map(fieldName -> {
                                Map<String, Object> fieldPropertyMap = new LinkedHashMap<>();
                                fieldPropertyMap.put(JCR_TITLE, fieldName);
                                fieldPropertyMap.put(SORTABLE, true);
                                return fieldPropertyMap;
                            }
                    ).collect(Collectors.toList()));

            return DataSourceUtils.createColumnsResourceList(
                    resourceResolver,
                    EMPTY,
                    fieldList);
        } catch (LoginException e) {
            throw new BullseyeException(
                    "Error logging in with the service user.",
                    "There was a problem fetching the table columns.",
                    e);
        }
    }

    @Override
    public void deleteEntities(final List<String> entityIds) {
        targetApiService.deleteEntities(entityIds, null);
    }

}
