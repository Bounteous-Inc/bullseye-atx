package com.bullseyeaem.core.antiflicker.servlets.ds;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.bullseyeaem.core.antiflicker.services.AntiFlickerService;
import com.bullseyeaem.core.common.ds.PaginatedDataSource;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;

import static com.bullseyeaem.core.antiflicker.servlets.ds.AntiFlickerConfigsDataSourceServlet.*;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.math.NumberUtils.createInteger;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_METHODS + "=" + METHOD_GET,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_TABLE_ITEMS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_STAGING_IDENTIFIERS,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_SHARED_JAVASCRIPT_SNIPPETS})
public class AntiFlickerConfigsDataSourceServlet extends SlingSafeMethodsServlet {
    private static final long serialVersionUID = 3870358008183572390L;
    static final String RESOURCE_TYPE_TABLE_ITEMS = "bullseye-aem/components/content/anti-flicker/table-items";
    static final String RESOURCE_TYPE_STAGING_IDENTIFIERS = "bullseye-aem/components/content/anti-flicker/staging-identifiers";
    static final String RESOURCE_TYPE_SHARED_JAVASCRIPT_SNIPPETS = "bullseye-aem/components/content/anti-flicker/shared-javascript-snippets";
    private static final int DEFAULT_LIMIT = 40;

    @Reference
    private AntiFlickerService antiFlickerService;

    @Override
    public void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        final Resource resource = request.getResource();
        if (RESOURCE_TYPE_TABLE_ITEMS.equals(resource.getResourceType())) {
            doConfigSelectOptionsList(request, response);
        } else if (RESOURCE_TYPE_STAGING_IDENTIFIERS.equals(resource.getResourceType())) {
            doStagingIdentifierSelectOptionsList(request, response);
        } else if (RESOURCE_TYPE_SHARED_JAVASCRIPT_SNIPPETS.equals(resource.getResourceType())) {
            doSharedJavascriptSnippetSelectOptionsList(request, response);
        }
    }

    private void doConfigSelectOptionsList(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final String[] selectors = request.getRequestPathInfo().getSelectors();

        final Integer offset = (selectors.length > 0) ? defaultIfNull(createInteger(selectors[0]), 0) : 0;
        final Integer limit = (selectors.length > 1) ?
                defaultIfNull(createInteger(selectors[1]), DEFAULT_LIMIT) :
                request.getResource().getValueMap().get("limit", DEFAULT_LIMIT);

        request.setAttribute(DataSource.class.getName(), new PaginatedDataSource(antiFlickerService.getAllConfigSelectOptionsList().iterator(), offset, limit));
    }

    private void doStagingIdentifierSelectOptionsList(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(DataSource.class.getName(), new SimpleDataSource(antiFlickerService.getStagingIdentifierSelectOptionsList().iterator()));
    }

    private void doSharedJavascriptSnippetSelectOptionsList(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(DataSource.class.getName(), new SimpleDataSource(antiFlickerService.getSharedJavascriptSnippetSelectOptionsList().iterator()));
    }
}
