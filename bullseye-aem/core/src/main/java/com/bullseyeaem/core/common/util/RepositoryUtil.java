package com.bullseyeaem.core.common.util;

import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.bullseyeaem.core.common.util.BullseyeConstants.CONF_ROOT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.wcm.api.constants.NameConstants.NT_PAGE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.NT_SLING_FOLDER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.NT_SLING_ORDERED_FOLDER;

public class RepositoryUtil {

    public static boolean isFolder(final Resource resource) {
        return (resource != null) &&
                (resource.getResourceType().equals(NT_SLING_FOLDER) ||
                        resource.getResourceType().equals(NT_SLING_ORDERED_FOLDER));
    }

    public static boolean isPage(final Resource resource) {
        return resource != null && resource.getValueMap().get(JCR_PRIMARYTYPE, EMPTY).equals(NT_PAGE);
    }

    public static boolean isConfRootFolder(final Resource resource) {
        return isFolder(resource) && CONF_ROOT.equals(resource.getPath());
    }

    public static boolean isConfFolder(final Resource resource) {
        return isFolder(resource) && isConfRootFolder(resource.getParent());
    }

    public static Resource getConfFolderResource(final Resource resource) {
        if (isConfFolder(resource) || resource == null) {
            return resource;
        }
        return getConfFolderResource(resource.getParent());
    }

    public static Resource getConfRootFolderResource(final Resource resource) {
        if (isConfRootFolder(resource) || resource == null) {
            return resource;
        }
        return getConfRootFolderResource(resource.getParent());
    }

    public static List<Resource> findAllFilteredResources(final Resource rootFolderResource,
                                                       final Predicate<Resource> filterPredicate) {

        final List<Resource> filteredResourceList = new ArrayList<>();

        if (rootFolderResource == null) {
            return filteredResourceList;
        }

        final Iterable<Resource> childResourceIterator = rootFolderResource.getChildren();
        for (Resource childResource : childResourceIterator) {
            if (filterPredicate == null || filterPredicate.test(childResource)) {
                filteredResourceList.add(childResource);
            } else if (isFolder(childResource)) {
                filteredResourceList.addAll(findAllFilteredResources(childResource, filterPredicate));
            }
        }

        return filteredResourceList;
    }

    public static Resource findFirstFilteredResources(final Resource rootFolderResource,
                                                          final Predicate<Resource> filterPredicate) {
        if (rootFolderResource == null) {
            return null;
        }

        Resource firstFilteredResource = null;
        final Iterable<Resource> childResourceIterator = rootFolderResource.getChildren();
        for (Resource childResource : childResourceIterator) {
            if (filterPredicate == null || filterPredicate.test(childResource)) {
                firstFilteredResource = childResource;
            } else if (isFolder(childResource)) {
                firstFilteredResource = findFirstFilteredResources(childResource, filterPredicate);
            }

            if (firstFilteredResource != null) {
                return firstFilteredResource;
            }
        }

        return null;
    }
}
