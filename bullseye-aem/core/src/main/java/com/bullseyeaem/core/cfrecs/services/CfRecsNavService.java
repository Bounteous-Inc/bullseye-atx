package com.bullseyeaem.core.cfrecs.services;

import com.adobe.granite.ui.components.ExpressionHelper;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface CfRecsNavService {
    List<Resource> getNavTableItemsResourceList(final Resource folderResource);
    List<Resource> getNavTableColumnsResourceList(final Resource folderResource);
    List<Resource> getNavBreadcrumbsResourceList(final String appURI, final Resource folderResource);
}
