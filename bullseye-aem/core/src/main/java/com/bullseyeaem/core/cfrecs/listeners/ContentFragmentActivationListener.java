package com.bullseyeaem.core.cfrecs.listeners;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.bullseyeaem.core.cfrecs.jobs.CfRecsJobExecutor;
import com.bullseyeaem.core.cfrecs.services.CfRecsMappingsService;
import com.bullseyeaem.core.common.exceptions.BullseyeException;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.bullseyeaem.core.cfrecs.util.CfRecsConstants.CF_RECS_SUBSERVICE;
import static com.day.cq.dam.api.DamConstants.MOUNTPOINT_ASSETS;
import static org.apache.sling.api.resource.ResourceResolverFactory.SUBSERVICE;

@Component(service = EventHandler.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Listens for activations of content fragments",
                EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC
        })
@Designate(ocd = ContentFragmentActivationListener.Config.class)
public class ContentFragmentActivationListener implements EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ContentFragmentActivationListener.class);
    private boolean enabled = false;

    @Reference
    private JobManager jobManager;

    @Reference
    private CfRecsMappingsService cfRecsMappingsService;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void handleEvent(Event event) {
        ReplicationAction action = ReplicationAction.fromEvent(event);

        if (action != null && action.getPath().startsWith(MOUNTPOINT_ASSETS)) {
            if (action.getType().equals(ReplicationActionType.ACTIVATE)) {
                final Map<String, Object> props = new HashMap<>();
                props.put(CfRecsJobExecutor.PROP_CF_PATHS, new String[] {action.getPath()});

                jobManager.addJob(CfRecsJobExecutor.CFRECS_PUSH_CF_TOPIC, props);
            } else if (action.getType().equals(ReplicationActionType.DEACTIVATE)) {
                try {
                    final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, CF_RECS_SUBSERVICE));

                    final Resource cfResource = resourceResolver.getResource(action.getPath());
                    if (cfResource != null) {
                        final ContentFragment contentFragment = cfResource.adaptTo(ContentFragment.class);
                        if (contentFragment != null) {
                            final Map<String, Object> props = new HashMap<>();
                            props.put(CfRecsJobExecutor.PROP_ENTITY_IDS, new String[]{cfRecsMappingsService.getEntityIdForContentFragmentResource(cfResource)});

                            LOG.debug("Firing off replication event for {}", action.getPath());
                            jobManager.addJob(CfRecsJobExecutor.CFRECS_DELETE_ENTITIES_TOPIC, props);
                        }
                    }

                } catch (LoginException e) {
                    throw new BullseyeException(
                            "Error logging in with the service user.",
                            "There was a problem deleting the entities from Target.",
                            e);
                }
            }
        }
    }

    @Activate
    public void activate(Config config) {
        enabled = config.enabled();
    }

    @ObjectClassDefinition(name = "Bullseye AEM - Content Fragment Change Listener",
            description = "Watches for changes to content fragments and queues the changes to Adobe Target")
    @interface Config {
        @AttributeDefinition(
                name = "Enabled",
                description = "True/false flag to activate or suspend this service, off by default"
        )
        boolean enabled();
    }
}
