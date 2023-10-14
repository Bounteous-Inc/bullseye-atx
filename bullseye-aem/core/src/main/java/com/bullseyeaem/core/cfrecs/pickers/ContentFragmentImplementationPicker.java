package com.bullseyeaem.core.cfrecs.pickers;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.bullseyeaem.core.cfrecs.annotations.ContentFragmentSlingModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.ImplementationPicker;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import java.util.Objects;
import java.util.Optional;

import static com.day.crx.JcrConstants.JCR_CONTENT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component(property = Constants.SERVICE_RANKING + ":Integer=-1", service = ImplementationPicker.class)
public class ContentFragmentImplementationPicker implements ImplementationPicker {

    @Override
    public Class<?> pick(@NotNull Class<?> adapterType, @NotNull Class<?>[] implementationTypes, @NotNull Object adaptable) {
        final Resource resource = findResource(adaptable);

        String resourceCfmPath = Optional.ofNullable(resource)
                .map(cfResource -> cfResource.adaptTo(ContentFragment.class))
                .filter(Objects::nonNull)
                .map(ContentFragment::getTemplate)
                .map(template -> template.adaptTo(Resource.class))
                .map(Resource::getPath)
                .orElse(StringUtils.EMPTY);

        if (isNotBlank(resourceCfmPath)) {
            for (Class<?> clazz : implementationTypes) {
                final ContentFragmentSlingModel cfSlingModelAnnotation = clazz.getAnnotation(ContentFragmentSlingModel.class);

                if (cfSlingModelAnnotation != null) {
                    final String clazzCfModelPath = cfSlingModelAnnotation.value();
                    if (resourceCfmPath.equals(clazzCfModelPath) || resourceCfmPath.equals(clazzCfModelPath + "/" + JCR_CONTENT)) {
                        // TODO: Cache this in a map somewhere similar to how org.apache.sling.models.impl.ResourceTypeBasedResourcePicker
                        // caches in AdapterImplementations
                        return clazz;
                    }
                }
            }
        }

        return null;
    }

    private Resource findResource(Object adaptable) {
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        } else if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        } else {
            return null;
        }
    }
}
