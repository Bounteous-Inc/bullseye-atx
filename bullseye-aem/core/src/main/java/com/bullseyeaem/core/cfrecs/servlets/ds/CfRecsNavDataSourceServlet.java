package com.bullseyeaem.core.cfrecs.servlets.ds;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.bullseyeaem.core.cfrecs.services.CfRecsNavService;
import com.bullseyeaem.core.cfrecs.servlets.AbstractCfRecsSafeMethodsServlet;
import com.bullseyeaem.core.common.ds.PaginatedDataSource;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;

import static com.bullseyeaem.core.cfrecs.servlets.ds.CfRecsNavDataSourceServlet.*;
import static com.bullseyeaem.core.common.util.BullseyeConstants.CONF_ROOT;
import static com.bullseyeaem.core.common.util.RepositoryUtil.isConfRootFolder;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.math.NumberUtils.createInteger;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_METHODS + "=" + METHOD_GET,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_NAV_TABLE_ITEMS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_NAV_TABLE_COLUMNS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_BREADCRUMBS})
public class CfRecsNavDataSourceServlet extends AbstractCfRecsSafeMethodsServlet {
    private static final long serialVersionUID = 3323594328637060146L;

    static final String RESOURCE_TYPE_NAV_TABLE_ITEMS = "bullseye-aem/components/content/cf-recs/nav/table-items";
    static final String RESOURCE_TYPE_NAV_TABLE_COLUMNS = "bullseye-aem/components/content/cf-recs/nav/table-columns";
    static final String RESOURCE_TYPE_BREADCRUMBS = "bullseye-aem/components/content/cf-recs/breadcrumbs";
    private static final int DEFAULT_LIMIT = 40;
    private static final String LIMIT = "limit";
    private static final String PATH = "path";
    private static final String TITLE = "title";
    private static final String IS_FOLDER = "isFolder";

    @Reference
    private CfRecsNavService cfRecsNavService;

    @Override
    public void doGet(SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        final String resourceType = request.getResource().getResourceType();

        switch (resourceType) {
            case RESOURCE_TYPE_NAV_TABLE_ITEMS:
                doTableItems(request, response);
                break;
            case RESOURCE_TYPE_NAV_TABLE_COLUMNS:
                doTableColumns(request, response);
                break;
            case RESOURCE_TYPE_BREADCRUMBS:
                doBreadCrumbs(request, response);
                break;
        }
    }

    private void doTableItems(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final String[] selectors = request.getRequestPathInfo().getSelectors();

        final Integer offset = (selectors.length > 0) ? defaultIfNull(createInteger(selectors[0]), 0) : 0;
        final Integer limit = (selectors.length > 1) ?
                defaultIfNull(createInteger(selectors[1]), DEFAULT_LIMIT) :
                request.getResource().getValueMap().get(LIMIT, DEFAULT_LIMIT);

        request.setAttribute(
                DataSource.class.getName(),
                new PaginatedDataSource(
                        cfRecsNavService.getNavTableItemsResourceList(getCurrentFolder(request))
                                .iterator(), offset, limit));
    }

    private void doTableColumns(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(
                        cfRecsNavService.getNavTableColumnsResourceList(getCurrentFolder(request))
                                .iterator()));
    }

    private void doBreadCrumbs(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(cfRecsNavService.getNavBreadcrumbsResourceList(
                                request.getRequestURI(), getCurrentFolder(request))
                        .iterator()));
    }

    private Resource getCurrentFolder(final SlingHttpServletRequest request) {
        final Resource currentFolderResource = request.getRequestPathInfo().getSuffixResource();
        if (currentFolderResource != null) {
            final Resource parentFolderResource = currentFolderResource.getParent();
            if (isConfRootFolder(parentFolderResource)) {
                return currentFolderResource;
            }
        }

        return request.getResourceResolver().getResource(CONF_ROOT);
    }

}
