package com.bullseyeaem.core.cfrecs.services.impl;

import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.TargetEnvironment;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.TargetRecsEntity;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch.TargetRecsCatalogSearchQuery;
import com.bullseyeaem.core.cfrecs.models.adobeio.adobetargetrecs.catalogsearch.TargetRecsCatalogSearchResult;
import com.bullseyeaem.core.cfrecs.services.TargetApiService;
import com.bullseyeaem.core.common.exceptions.BullseyeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.http.HttpHeaders.*;

@Component(
        service = TargetApiService.class
)
@Designate(ocd = TargetApiServiceImpl.Config.class)
public class TargetApiServiceImpl implements TargetApiService {
    private static final Logger LOG = LoggerFactory.getLogger(TargetApiServiceImpl.class);

    static final String TARGET_RECS_CONTENT_TYPE = "application/vnd.adobe.target.v1+json";
    static final String ADOBE_IO_CONTENT_TYPE = "application/x-www-form-urlencoded";
    static final String ADOBE_IO_AUTH_TOKEN_URL = "https://ims-na1.adobelogin.com/ims/token/v3";
    static final String TARGET_API_DOMAIN = "https://mc.adobe.io";
    static final String TARGET_ENVIRONMENTS_API_PATH = "/target/environments";
    static final String TARGET_RECS_ENTITIES_API_PATH = "/target/recs/entities";
    static final String TARGET_RECS_CATALOG_SEARCH_API_PATH = "/target/recs/catalogsearch";
    private static final String PARAM_GRANT_TYPE = "grant_type";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String PARAM_SCOPE = "scope";
    private static final String VALUE_SCOPE = "openid,AdobeID,target_sdk,additional_info.projectedProductContext,read_organizations,additional_info.roles";
    private static final String VALUE_GRANT_TYPE = "client_credentials";
    private static final String X_API_KEY = "X-Api-Key";
    private boolean enabled;
    private String defaultEnvironmentId;
    private String tenantId;
    private String apiKey;
    private String clientSecret;

    private OAuthAccessToken oAuthAccessToken;

    @Override
    public TargetRecsCatalogSearchResult searchTargetRecsEntities(final TargetRecsCatalogSearchQuery targetRecsCatalogSearchQuery) {
        if (targetRecsCatalogSearchQuery == null) {
            return null;
        }

        final Gson gson = new Gson();

        try {
            HttpRequest newRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TARGET_API_DOMAIN + "/" + tenantId + TARGET_RECS_CATALOG_SEARCH_API_PATH))
                    .header(ACCEPT, TARGET_RECS_CONTENT_TYPE)
                    .header(CONTENT_TYPE, TARGET_RECS_CONTENT_TYPE)
                    .header(AUTHORIZATION, "Bearer " + getAccessToken())
                    .header(X_API_KEY, this.apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(targetRecsCatalogSearchQuery)))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> apiResponse = httpClient.send(newRequest, HttpResponse.BodyHandlers.ofString());

            return gson.fromJson(apiResponse.body(), TargetRecsCatalogSearchResult.class);

            /*
            List<Map<String,?>> hitsList = (List<Map<String, ?>>) bodyMap.get("hits");

            return hitsList.stream()
                    .map(hit -> {
                        String name = hit.containsKey("name") ? (String) hit.get("name") : "NA";
                        String id = hit.containsKey("id") ? (String) hit.get("id") : "NA";
                        //TODO: Add categories from Map
                        List<String> categories = List.of();

                        TargetRecsEntityAttributes standardAttributes = new TargetRecsEntityAttributes(
                                hit.containsKey("message") ? (String) hit.get("message") : "NA",
                                hit.containsKey("thumbnailUrl") ? (String) hit.get("thumbnailUrl") : "NA",
                                null,
                                hit.containsKey("pageUrl") ? (String) hit.get("pageUrl") : "NA",
                                null,
                                null
                        );

                        //TODO: Populate customAttributes from Map
                        Map<String,Object> customAttributes = Map.of();

                        return new TargetRecsEntity(
                                name,
                                id,
                                categories,
                                standardAttributes,
                                customAttributes,
                                true
                        );
                    }).collect(Collectors.toList());
             */
        } catch (Exception e) {
            LOG.error("Error pushing Content Fragments to Target Recommendations");
        }

        return null;
    }

    @Override
    public void pushTargetRecsEntities(List<TargetRecsEntity> targetRecsEntityList, Integer targetEnvironmentId) {
        if (targetRecsEntityList.isEmpty()) {
            LOG.warn("No entities provided to save");
            return;
        }

        //final Gson gson = new Gson();
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> bodyMap = new HashMap<>();

        for (TargetRecsEntity entity: targetRecsEntityList) {
            entity.setEnvironment((targetEnvironmentId != null) ? targetEnvironmentId : Integer.parseInt(this.defaultEnvironmentId));
        }
        bodyMap.put("entities", targetRecsEntityList);

        try {
            final String requestJson = mapper.writeValueAsString(bodyMap);

            HttpRequest newRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TARGET_API_DOMAIN + "/" + tenantId + TARGET_RECS_ENTITIES_API_PATH))
                    .header(ACCEPT, TARGET_RECS_CONTENT_TYPE)
                    .header(CONTENT_TYPE, TARGET_RECS_CONTENT_TYPE)
                    .header(AUTHORIZATION, "Bearer " + getAccessToken())
                    .header(X_API_KEY, this.apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> apiResponse = httpClient.send(newRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            LOG.error("Error pushing Content Fragments to Target Recommendations");
        }
    }

    @Override
    public List<TargetEnvironment> getTargetEnvironments() {
        return List.of();
    }

    public void deleteEntities(final List<String> entityIdList, final Integer targetEnvironmentId) {
        try {
            final int environmentId = (targetEnvironmentId != null) ? targetEnvironmentId : Integer.parseInt(this.defaultEnvironmentId);

            HttpRequest newRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TARGET_API_DOMAIN + "/" + tenantId + TARGET_RECS_ENTITIES_API_PATH + "?ids=" + String.join(",", entityIdList) + "&environment=" + environmentId))
                    .header(ACCEPT, TARGET_RECS_CONTENT_TYPE)
                    .header(CONTENT_TYPE, TARGET_RECS_CONTENT_TYPE)
                    .header(AUTHORIZATION, "Bearer " + getAccessToken())
                    .header(X_API_KEY, this.apiKey)
                    .DELETE()
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> apiResponse = httpClient.send(newRequest, HttpResponse.BodyHandlers.ofString());
            LOG.info("Entities deleted");
        } catch (Exception e) {
            LOG.error("Error pushing Content Fragments to Target Recommendations");
        }
    }

    private String getAccessToken() {
        if (this.oAuthAccessToken != null) {
            if (this.oAuthAccessToken.isExpired()) {
                this.oAuthAccessToken = null;
            } else {
                return this.oAuthAccessToken.getAccessToken();
            }
        }

        final Gson gson = new Gson();
        final Map<String, String> bodyMap = Map.of(
                PARAM_GRANT_TYPE, VALUE_GRANT_TYPE,
                PARAM_CLIENT_ID, this.apiKey,
                PARAM_CLIENT_SECRET, this.clientSecret,
                PARAM_SCOPE, VALUE_SCOPE);

        try {
            HttpRequest newRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ADOBE_IO_AUTH_TOKEN_URL))
                    .header(ACCEPT, "*/*")
                    .header(CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .POST(getParamsUrlEncoded(bodyMap))
                    .build();

            final HttpClient httpClient = HttpClient.newHttpClient();
            final HttpResponse<String> apiResponse = httpClient.send(newRequest, HttpResponse.BodyHandlers.ofString());
            final JsonObject responseBodyJsonObject = gson.fromJson(apiResponse.body(), JsonObject.class);
            this.oAuthAccessToken = new OAuthAccessToken(
                    responseBodyJsonObject.get("access_token").getAsString(),
                    responseBodyJsonObject.get("expires_in").getAsInt());

            return this.oAuthAccessToken.getAccessToken();
        } catch (Exception e) {
            //TODO: Retry if haven't already
            LOG.error("Error pushing Content Fragments to Target Recommendations");
            throw new BullseyeException("Unable to authenticate with service.", e);
        }
    }

    private HttpRequest.BodyPublisher getParamsUrlEncoded(Map<String, String> parameters) {
        String urlEncoded = parameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return HttpRequest.BodyPublishers.ofString(urlEncoded);
    }

    @Activate
    public void activate(TargetApiServiceImpl.Config config) {

        this.enabled = config.enabled();
        this.defaultEnvironmentId = config.defaultEnvironmentId();
        this.tenantId = config.tenantId();
        this.apiKey = config.apiKey();
        this.clientSecret = config.clientSecret();;
    }

    @ObjectClassDefinition(name = "Bullseye AEM - Target API Service",
            description = "Facade service to the Adobe Target REST APIs.")
    @interface Config {
        @AttributeDefinition(
                name = "Enabled",
                description = "True/false flag to activate or suspend this service, off by default"
        )
        boolean enabled();

        @AttributeDefinition(
                name = "Default Environment Id",
                description = "The numeric Id from Adobe Target for the destination environment"
        )
        String defaultEnvironmentId();

        @AttributeDefinition(
                name = "Tenant Id",
                description = "The Adobe Experience Cloud tenantId for this environment"
        )
        String tenantId();

        @AttributeDefinition(
                name = "API Key",
                description = "The API Key to connect to the Adobe IO Target service."
        )
        String apiKey();

        @AttributeDefinition(
                name = "Client Secret",
                description = "The Client Secret to connect to the Adobe IO Target service."
        )
        String clientSecret();
    }

    static class OAuthAccessToken {
        // Get a new access token slightly before the actual expiration just to be safe.
        private static final int EXPIRATION_BUFFER = 10;  // seconds
        private final String accessToken;
        private final Calendar expiration;

        public OAuthAccessToken(final String accessToken, final int expiresIn) {
            this.accessToken = accessToken;
            final Calendar now = Calendar.getInstance();
            now.add(Calendar.MILLISECOND, ((expiresIn - EXPIRATION_BUFFER) * 1000));
            this.expiration = now;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public boolean isExpired() {
            return Calendar.getInstance().after(this.expiration);
        }
    }
}


