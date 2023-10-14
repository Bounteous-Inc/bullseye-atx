package com.bullseyeaem.core.cfrecs.predicates;

import com.adobe.cq.dam.cfm.ContentFragment;
import org.apache.sling.api.resource.Resource;

import java.util.Optional;
import java.util.function.Predicate;

import static com.day.crx.JcrConstants.JCR_CONTENT;

/**
 * This predicate can be used to verify that a given Resource is a Content Fragment of a given model type.
 */
public class ContentFragmentIsOfModelTypePredicate implements Predicate<Resource> {
    private final String cfModelPath;

    public ContentFragmentIsOfModelTypePredicate(final String cfModelPath) {
        this.cfModelPath = cfModelPath;
    }

    @Override
    public boolean test(Resource cfResource) {
        return Optional.ofNullable(cfResource)
                .map(res -> res.adaptTo(ContentFragment.class))
                .map(ContentFragment::getTemplate)
                .map(fragmentTemplate -> fragmentTemplate.adaptTo(Resource.class))
                .map(Resource::getPath)
                .map(path -> path.equals(cfModelPath + "/" + JCR_CONTENT))
                .orElse(false);
    }
}
