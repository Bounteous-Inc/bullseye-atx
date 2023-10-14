package com.bullseyeaem.core.cfrecs.servlets;

import com.bullseyeaem.core.cfrecs.jobs.CfRecsJobExecutor;
import com.bullseyeaem.core.cfrecs.services.CfRecsEntitiesService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.event.jobs.JobManager;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bullseyeaem.core.cfrecs.servlets.CfRecsActionsServlet.ACTION_PUSH_TO_TARGET_PATH;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_POST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_METHODS + "=" + METHOD_POST,
        SLING_SERVLET_PATHS + "=" + ACTION_PUSH_TO_TARGET_PATH})
public class CfRecsActionsServlet extends SlingAllMethodsServlet {
    private static final long serialVersionUID = 6662727806446511436L;
    private static final Logger LOG = LoggerFactory.getLogger(CfRecsActionsServlet.class);
    private static final String PARAM_PATHS = "paths";
    private static final String PARAM_IDS = "ids";
    private static final String MAPPING_PATHS = "mappingPaths";
    static final String ACTION_PUSH_TO_TARGET_PATH = "/services/bullseye-aem/cf-recs/push-to-target";
    static final String ACTION_DELETE_ENTITIES_PATH = "/services/bullseye-aem/cf-recs/delete-entities";

    @Reference
    private JobManager jobManager;

    @Reference
    private CfRecsEntitiesService cfRecsEntitiesService;

    @Override
    public void doPost(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {

        if (ACTION_PUSH_TO_TARGET_PATH.equals(request.getPathInfo())) {
            final String[] mappingPaths = request.getParameterValues(PARAM_PATHS);

            final Map<String, Object> props = new HashMap<>();
            props.put(MAPPING_PATHS, mappingPaths);

            jobManager.createJob(CfRecsJobExecutor.CFRECS_PUSH_CF_MAPPINGS_TOPIC).properties(props).add();
        }
    }

    @Override
    public void doDelete(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        if (ACTION_DELETE_ENTITIES_PATH.equals(request.getPathInfo())) {
            final String[] entityIds = request.getParameterValues(PARAM_IDS);
            cfRecsEntitiesService.deleteEntities(List.of(entityIds));
        }
    }
}
