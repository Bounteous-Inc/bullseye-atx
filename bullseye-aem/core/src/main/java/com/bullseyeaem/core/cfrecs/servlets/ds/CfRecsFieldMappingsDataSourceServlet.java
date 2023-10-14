package com.bullseyeaem.core.cfrecs.servlets.ds;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.bullseyeaem.core.cfrecs.services.CfRecsMappingsService;
import com.bullseyeaem.core.cfrecs.servlets.AbstractCfRecsSafeMethodsServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.util.List;

import static com.bullseyeaem.core.cfrecs.servlets.ds.CfRecsFieldMappingsDataSourceServlet.*;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_METHODS + "=" + METHOD_GET,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_FIELD_MAPPING_TABLE_ITEMS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_CF_MODEL_ITEMS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_UNMAPPED_CF_MODEL_ITEMS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_MAPPED_CF_MODEL_ITEMS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_CF_MODEL_FIELD_ITEMS})
public class CfRecsFieldMappingsDataSourceServlet extends AbstractCfRecsSafeMethodsServlet {
    private static final long serialVersionUID = 318022906474864849L;

    static final String RESOURCE_TYPE_FIELD_MAPPING_TABLE_ITEMS = "bullseye-aem/components/content/cf-recs/field-mapping/table-items";
    static final String RESOURCE_TYPE_CF_MODEL_ITEMS = "bullseye-aem/components/content/cf-recs/field-mapping/cf-model-items";
    static final String RESOURCE_TYPE_UNMAPPED_CF_MODEL_ITEMS = "bullseye-aem/components/content/cf-recs/field-mapping/unmapped-cf-model-items";
    static final String RESOURCE_TYPE_MAPPED_CF_MODEL_ITEMS = "bullseye-aem/components/content/cf-recs/field-mapping/mapped-cf-model-items";
    static final String RESOURCE_TYPE_CF_MODEL_FIELD_ITEMS = "bullseye-aem/components/content/cf-recs/field-mapping/cf-model-field-items";

    @Reference
    CfRecsMappingsService cfRecsMappingsService;

    @Override
    public void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        final String resourceType = request.getResource().getResourceType();

        switch (resourceType) {
            case RESOURCE_TYPE_FIELD_MAPPING_TABLE_ITEMS:
                doTableItems(request, response);
                break;
            case RESOURCE_TYPE_CF_MODEL_ITEMS:
                doCfModelSelectOptions(request, response);
                break;
            case RESOURCE_TYPE_UNMAPPED_CF_MODEL_ITEMS:
                doUnmappedCfModelSelectOptions(request, response);
                break;
            case RESOURCE_TYPE_MAPPED_CF_MODEL_ITEMS:
                doMappedCfModelSelectOptions(request, response);
                break;
            case RESOURCE_TYPE_CF_MODEL_FIELD_ITEMS:
                doCfModelFieldSelectOptions(request, response);
                break;
        }
    }

    private void doTableItems(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(
                DataSource.class.getName(),
                        new SimpleDataSource(
                                cfRecsMappingsService.getMappingsTableItemsResourceList(
                                                getCurrentConfFolderResource(request))
                                        .iterator()));
    }

    private void doCfModelSelectOptions(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        List<Resource> optionsResourceList = cfRecsMappingsService.getCfModelSelectOptionsResourceList(
                getCurrentConfFolderResource(request),
                getCurrentCfModelPath(request));

        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(optionsResourceList.iterator()));
    }

    private void doUnmappedCfModelSelectOptions(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        List<Resource> optionsResourceList = cfRecsMappingsService.getUnmappedCfModelSelectOptionsResourceList(
                    getCurrentConfFolderResource(request),
                    getCurrentCfModelPath(request));

        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(optionsResourceList.iterator()));
    }

    private void doMappedCfModelSelectOptions(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        List<Resource> optionsResourceList = cfRecsMappingsService.getMappedCfModelSelectOptionsResourceList(
                getCurrentConfFolderResource(request),
                getCurrentCfModelPath(request));

        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(optionsResourceList.iterator()));
    }

    private void doCfModelFieldSelectOptions(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(cfRecsMappingsService.getCfModelFieldSelectOptionsResourceList(
                                getCurrentConfFolderResource(request),
                                getCurrentCfModelPath(request))
                        .iterator()));
    }
}
