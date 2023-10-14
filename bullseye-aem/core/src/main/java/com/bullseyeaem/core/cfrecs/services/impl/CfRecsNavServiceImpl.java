package com.bullseyeaem.core.cfrecs.services.impl;

import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.bullseyeaem.core.cfrecs.services.CfRecsNavService;
import com.bullseyeaem.core.common.exceptions.BullseyeException;
import com.bullseyeaem.core.common.util.DataSourceUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.CF_RECS_SUBSERVICE;
import static com.bullseyeaem.core.common.util.BullseyeConstants.*;
import static com.bullseyeaem.core.common.util.RepositoryUtil.*;
import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.day.cq.wcm.api.constants.NameConstants.PN_SLING_VANITY_PATH;
import static com.day.crx.JcrConstants.NT_UNSTRUCTURED;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.sling.api.resource.ResourceResolverFactory.SUBSERVICE;

@Component(
        service = CfRecsNavService.class
)
public class CfRecsNavServiceImpl implements CfRecsNavService {

    static final String RESOURCE_TYPE_UTILITIES_FOLDER_ITEM = "bullseye-aem/utilities/cf-recs/components/folder-item";
    static final String RESOURCE_TYPE_UTILITIES_FEATURE_ITEM = "bullseye-aem/utilities/cf-recs/components/feature-item";
    static final String CF_RECS_UTILITY_PATH = "/apps/bullseye-aem/utilities/cf-recs";
    static final String FEATURES_FOLDER_PATH = CF_RECS_UTILITY_PATH + "/features";
    private static final String ROOT_BREADCRUMBS_TITLE = "CF Recs";
    private static final String NAME = "name";
    private static final String HREF = "href";
    private static final String ICON = "icon";
    private static final String PATH = "path";
    private static final String TITLE = "title";
    private static final String IS_FOLDER = "isFolder";
    private static final String ICON_GRAPH_PATHING = "graphPathing";
    private static final String ROOT_PATH = "rootPath";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public List<Resource> getNavTableItemsResourceList(final Resource folderResource) {

        final List<Resource> navTableItemsResourceList = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            if (isConfRootFolder(folderResource)) {
                final Iterator<Resource> childIterator = folderResource.listChildren();
                while (childIterator.hasNext()) {
                    final Resource childResource = childIterator.next();
                    if (isConfFolder(childResource)) {
                        final ValueMap childValueMap = childResource.getValueMap();
                        final ValueMap newValueMap = new ValueMapDecorator(Map.of(
                                NAME, childValueMap.get(JCR_TITLE, childResource.getName()),
                                HREF, CF_RECS_UTILITY_PATH + ".html" + childResource.getPath()
                        ));

                        final ValueMapResource valueMapResource = new ValueMapResource(
                                resourceResolver,
                                childResource.getPath(),
                                RESOURCE_TYPE_UTILITIES_FOLDER_ITEM,
                                newValueMap);

                        navTableItemsResourceList.add(valueMapResource);
                    }
                }
            } else if (isConfFolder(folderResource)) {
                final Resource featuresFolderResource = resourceResolver.getResource(FEATURES_FOLDER_PATH);
                if (featuresFolderResource != null) {
                    final Iterator<Resource> featuresIterator = featuresFolderResource.listChildren();
                    while (featuresIterator.hasNext()) {
                        final Resource featurePageResource = featuresIterator.next();
                        if (isPage(featurePageResource)) {
                            final DeepReadValueMapDecorator deepReadValueMap = new DeepReadValueMapDecorator(featurePageResource, featurePageResource.getValueMap());
                            final String href = deepReadValueMap.get(String.format("%s/%s", JCR_CONTENT, PN_SLING_VANITY_PATH), featurePageResource.getPath());

                            final ValueMap newValueMap = new ValueMapDecorator(Map.of(
                                    NAME, deepReadValueMap.get(String.format("%s/%s", JCR_CONTENT, JCR_TITLE), featurePageResource.getName()),
                                    HREF, href + ".html" + folderResource.getPath(),
                                    ICON, deepReadValueMap.get(String.format("%s/%s", JCR_CONTENT, ICON), ICON_GRAPH_PATHING)
                            ));

                            final ValueMapResource valueMapResource = new ValueMapResource(
                                    resourceResolver,
                                    featurePageResource.getPath(),
                                    RESOURCE_TYPE_UTILITIES_FEATURE_ITEM,
                                    newValueMap);

                            navTableItemsResourceList.add(valueMapResource);
                        }
                    }
                }
            }
        } catch (LoginException e) {
            throw new BullseyeException(
                    "Error logging in with the service user.",
                    "There was a problem fetching the table items.",
                    e);
        }

        return navTableItemsResourceList;
    }

    @Override
    public List<Resource> getNavTableColumnsResourceList(final Resource folderResource) {
        final List<Resource> resourceList = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            if (isConfRootFolder(folderResource) || isConfFolder(folderResource)) {
                resourceList.addAll(
                        DataSourceUtils.createColumnsResourceList(
                                resourceResolver,
                                folderResource.getPath(),
                                List.of(
                                        Map.of(
                                                JCR_TITLE, EMPTY,
                                                FIXED_WIDTH, true
                                        ),
                                        Map.of(
                                                JCR_TITLE, "Name",
                                                SORTABLE, true
                                        ))
                        ));
            }
        } catch (LoginException e) {
            throw new BullseyeException(
                    "Error logging in with the service user.",
                    "There was a problem fetching the table columns.",
                    e);
        }

        return resourceList;
    }

    @Override
    public List<Resource> getNavBreadcrumbsResourceList(final String appURI, final Resource folderResource) {
        final List<Resource> crumbs = new ArrayList<>();

        try {
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

            final Resource appResource = resourceResolver.resolve(appURI);
            final ValueMap currentAppValueMap = appResource.getValueMap();
            final String rootPath = currentAppValueMap.get(ROOT_PATH, CONF_ROOT);
            final boolean isFeaturePage = appResource.getPath().startsWith(FEATURES_FOLDER_PATH);

            if (isFeaturePage) {
                final ValueMap appValueMap = new DeepReadValueMapDecorator(appResource, appResource.getValueMap());
                final String title = appValueMap.get(String.format("%s/%s", JCR_CONTENT, JCR_TITLE), appResource.getName());
                final ValueMap crumbValueMap = new ValueMapDecorator(new HashMap<>());

                crumbValueMap.put(HREF, CF_RECS_UTILITY_PATH + ".html" + folderResource.getPath());
                crumbValueMap.put(TITLE, title);
                crumbValueMap.put(IS_FOLDER, true);
                crumbs.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), NT_UNSTRUCTURED, crumbValueMap));
            }

            Resource currentFolderResource = folderResource;
            while (currentFolderResource != null) {
                if (currentFolderResource.getPath().equals(rootPath)) {
                    break;
                }

                final ValueMap currentFolderValueMap = currentFolderResource.getValueMap();
                final String title = currentFolderValueMap.get(String.format("%s/%s", JCR_CONTENT, JCR_TITLE), currentFolderValueMap.get(JCR_TITLE, currentFolderResource.getName()));

                final ValueMap crumbValueMap = new ValueMapDecorator(new HashMap<>());

                if (isFeaturePage) {
                    crumbValueMap.put(HREF, CF_RECS_UTILITY_PATH + ".html" + currentFolderResource.getPath());
                } else {
                    crumbValueMap.put(PATH, currentFolderResource.getPath());
                }
                crumbValueMap.put(TITLE, title);
                crumbValueMap.put(IS_FOLDER, true);
                crumbs.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), NT_UNSTRUCTURED, crumbValueMap));

                currentFolderResource = currentFolderResource.getParent();
            }

            ValueMap crumbValueMap = new ValueMapDecorator(new HashMap<>());
            if (isFeaturePage) {
                crumbValueMap.put(HREF, CF_RECS_UTILITY_PATH + ".html" + CONF_ROOT);
            } else {
                crumbValueMap.put(PATH, CONF_ROOT);
            }
            crumbValueMap.put(TITLE, ROOT_BREADCRUMBS_TITLE);
            crumbValueMap.put(IS_FOLDER, true);
            crumbs.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), NT_UNSTRUCTURED, crumbValueMap));
        }  catch (LoginException e) {
            throw new BullseyeException(
                    "Error logging in with the service user.",
                    "There was a problem fetching the breadcrumbs.",
                    e);
        }

        return crumbs;
    }

}
