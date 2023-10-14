package com.bullseyeaem.core.cfrecs.services;

import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.TargetEnvironment;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch.TargetRecsCatalogSearchQuery;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch.TargetRecsCatalogSearchResult;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.TargetRecsEntity;

import java.util.List;

public interface TargetApiService {

    TargetRecsCatalogSearchResult searchTargetRecsEntities(TargetRecsCatalogSearchQuery targetRecsCatalogSearchQuery);

    void pushTargetRecsEntities(List<TargetRecsEntity> targetRecsEntities, Integer targetEnvironmentId);

    void deleteEntities(List<String> ids, Integer targetEnvironmentId);

    List<TargetEnvironment> getTargetEnvironments();

}
