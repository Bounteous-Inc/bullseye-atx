package com.bullseyeaem.core.cfrecs.services;

import org.apache.sling.api.resource.Resource;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface CfRecsEntitiesService {

    List<Resource> getEntitiesTableItemsResourceList(final Resource confFolderResource);

    List<Resource> getEntitiesTableColumnsResourceList();

    void deleteEntities(final List<String> entityIds);
}
