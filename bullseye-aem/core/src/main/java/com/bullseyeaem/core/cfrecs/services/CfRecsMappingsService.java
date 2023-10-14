package com.bullseyeaem.core.cfrecs.services;

import com.bullseyeaem.core.cfrecs.models.CfRecsFieldMapping;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.event.jobs.Job;

import java.util.List;
import java.util.Set;

public interface CfRecsMappingsService {
    List<Resource> getMappingsTableItemsResourceList(final Resource confFolderResource);

    List<Resource> getCfModelSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath);

    public List<Resource> getMappedCfModelSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath);

    public List<Resource> getUnmappedCfModelSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath);

    public List<Resource> getFieldMappingSelectOptionsResourceList(final Resource confFolderResource);

    List<Resource> getCfModelFieldSelectOptionsResourceList(final Resource confFolderResource, final String cfModelPath);

    void pushMappingsToTarget(final String mappingPath);

    void pushContentFragmentsToTarget(List<String> cfPaths);

    List<String> getMappedTargetFieldNameList(final Resource confFolderResource);

    Set<String> getMappedTargetFieldNameList(final Resource confFolderResource, final String cfModelPath);

    Job startJob();

    CfRecsFieldMapping findFieldMappingForContentFragment(final Resource cfResource, final ResourceResolver resourceResolver);

    String getEntityIdForContentFragmentResource(final Resource cfResource);

    void deleteMappings(String... mappingPaths);
}
