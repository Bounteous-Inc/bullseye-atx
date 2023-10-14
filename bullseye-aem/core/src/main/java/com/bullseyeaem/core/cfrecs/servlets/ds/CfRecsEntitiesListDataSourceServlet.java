package com.bullseyeaem.core.cfrecs.servlets.ds;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.bullseyeaem.core.cfrecs.services.CfRecsEntitiesService;
import com.bullseyeaem.core.cfrecs.servlets.AbstractCfRecsSafeMethodsServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bullseyeaem.core.cfrecs.servlets.ds.CfRecsEntitiesListDataSourceServlet.RESOURCE_TYPE_ENTITIES_TABLE_COLUMNS;
import static com.bullseyeaem.core.cfrecs.servlets.ds.CfRecsEntitiesListDataSourceServlet.RESOURCE_TYPE_ENTITIES_TABLE_ITEMS;
import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.STANDARD_TARGET_FIELD_NAMES;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_METHODS + "=" + METHOD_GET,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_ENTITIES_TABLE_COLUMNS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_ENTITIES_TABLE_ITEMS})
public class CfRecsEntitiesListDataSourceServlet extends AbstractCfRecsSafeMethodsServlet {
    private static final long serialVersionUID = -2038991011543043644L;

    static final String RESOURCE_TYPE_ENTITIES_TABLE_COLUMNS = "bullseye-aem/components/content/cf-recs/entities-list/table-columns";
    static final String RESOURCE_TYPE_ENTITIES_TABLE_ITEMS = "bullseye-aem/components/content/cf-recs/entities-list/table-items";

    @Reference
    private CfRecsEntitiesService cfRecsEntitiesService;

    @Override
    public void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        final String resourceType = request.getResource().getResourceType();

        switch (resourceType) {
            case RESOURCE_TYPE_ENTITIES_TABLE_COLUMNS:
                doTableColumns(request, response);
                break;
            case RESOURCE_TYPE_ENTITIES_TABLE_ITEMS:
                doTableItems(request, response);
                break;
        }
    }

    private void doTableColumns(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(
                        cfRecsEntitiesService.getEntitiesTableColumnsResourceList().iterator()));
    }

    private void doTableItems(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(
                        cfRecsEntitiesService.getEntitiesTableItemsResourceList(
                                        getCurrentConfFolderResource(request))
                                .iterator()));
    }

}
