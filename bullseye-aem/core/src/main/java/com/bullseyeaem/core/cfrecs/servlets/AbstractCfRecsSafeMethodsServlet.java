package com.bullseyeaem.core.cfrecs.servlets;

import com.bullseyeaem.core.cfrecs.models.CfRecsFieldMapping;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import static com.bullseyeaem.core.common.util.RepositoryUtil.isConfFolder;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract  class AbstractCfRecsSafeMethodsServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = -9067736412999113591L;

    protected static final String PARAM_SELECTED_CF_MODEL_PATH = "selectedCfModelPath";
    protected static final String PARAM_ITEM = "item";

    protected Resource getCurrentConfFolderResource(final SlingHttpServletRequest request) {
        // First check the suffix path for create mode
        Resource currentResource = request.getRequestPathInfo().getSuffixResource();

        while (currentResource != null && !isConfFolder(currentResource)) {
            currentResource = currentResource.getParent();
        }

        // Next check for the selectedCfModelPath parameter from wizard step
        if (currentResource == null) {
            currentResource = request.getResourceResolver().getResource(request.getParameter(PARAM_SELECTED_CF_MODEL_PATH));
            while (currentResource != null && !isConfFolder(currentResource)) {
                currentResource = currentResource.getParent();
            }
        }

        // Next try the item request parameter for edit mode
        if (currentResource == null) {
            currentResource = request.getResourceResolver().getResource(request.getParameter(PARAM_ITEM));
            while (currentResource != null && !isConfFolder(currentResource)) {
                currentResource = currentResource.getParent();
            }
        }

        return currentResource;
    }

    /**
     * Get the current content fragment model path, based on what was selected in the first step of a field mapping
     * creation wizard or the field value in an existing mapping.
     * @param request The HTTP request
     * @return the path of the current content fragment model
     */
    protected String getCurrentCfModelPath(final SlingHttpServletRequest request) {
        // In a field mapping creation wizard the selected content fragment model will be passed in the selectedCfModelPath parameter.
        String cfModelPath = request.getParameter(PARAM_SELECTED_CF_MODEL_PATH);

        // When editing a field mapping its path is passed in the item parameter.  From that we get the content fragment model path.
        if (isEmpty(cfModelPath)) {
            final String cfMappingPath = request.getParameter(PARAM_ITEM);
            final Resource cfMappingResource = request.getResourceResolver().getResource(cfMappingPath);
            if (cfMappingResource != null) {
                CfRecsFieldMapping cfRecsFieldMapping = cfMappingResource.adaptTo(CfRecsFieldMapping.class);
                if (cfRecsFieldMapping != null) {
                    cfModelPath = cfRecsFieldMapping.getCfModelPath();
                }
            }
        }

        return cfModelPath;
    }
}
