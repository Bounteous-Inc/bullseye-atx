package com.bullseyeaem.core.cfrecs.servlets.ds;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.bullseyeaem.core.cfrecs.services.CfRecsMappingsService;
import com.bullseyeaem.core.cfrecs.servlets.AbstractCfRecsSafeMethodsServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;

import static com.bullseyeaem.core.cfrecs.servlets.ds.CfRecsSchedulersDataSourceServlet.*;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_METHODS + "=" + METHOD_GET,
        SLING_SERVLET_RESOURCE_TYPES + "=" + RESOURCE_TYPE_SCHEDULER_CF_MODEL_SELECT_ITEMS})
public class CfRecsSchedulersDataSourceServlet extends AbstractCfRecsSafeMethodsServlet {
    private static final long serialVersionUID = 601530337476069176L;
    static final String RESOURCE_TYPE_SCHEDULER_CF_MODEL_SELECT_ITEMS = "bullseye-aem/cf-recs/scheduler/select/cf-model-items";

    @Reference
    CfRecsMappingsService cfRecsMappingsService;

    @Override
    public void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        final String resourceType = request.getResource().getResourceType();

        switch (resourceType) {
            case RESOURCE_TYPE_SCHEDULER_CF_MODEL_SELECT_ITEMS:
                doSchedulerCfModelSelectItems(request, response);
                break;
        }
    }

    private void doSchedulerCfModelSelectItems(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(cfRecsMappingsService.getFieldMappingSelectOptionsResourceList(
                                getCurrentConfFolderResource(request))
                        .iterator()));
    }
}
