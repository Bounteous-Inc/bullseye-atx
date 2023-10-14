package com.bullseyeaem.core.common.util;

import com.adobe.granite.ui.components.ds.ValueMapResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.day.crx.JcrConstants.NT_UNSTRUCTURED;

public class DataSourceUtils {
    /**
     * Turn an array of Map properties into a list of ValueMapResource objects with a nt:unstructured type.
     * @param resourceResolver a ResourceResolver to use in the returned Resource objects.
     * @param path a path to use for the new Resources.
     * @param mapList a list of maps containing the attributes and values for the new Resources.
     * @return a list of Resource objects
     */
    public static final List<Resource> createColumnsResourceList(final ResourceResolver resourceResolver, final String path, final List<Map<String, Object>> mapList) {
        return (mapList == null) ? new ArrayList<>() : mapList.stream()
                .map(columnMap -> new ValueMapResource(
                        resourceResolver,
                        path,
                        NT_UNSTRUCTURED,
                        new ValueMapDecorator(columnMap)))
                .collect(Collectors.toList());
    }
}
