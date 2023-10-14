package com.bullseyeaem.core.cfrecs.jobs;

import com.bullseyeaem.core.cfrecs.services.CfRecsEntitiesService;
import com.bullseyeaem.core.cfrecs.services.CfRecsMappingsService;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobExecutionContext;
import org.apache.sling.event.jobs.consumer.JobExecutionResult;
import org.apache.sling.event.jobs.consumer.JobExecutor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.bullseyeaem.core.cfrecs.jobs.CfRecsJobExecutor.*;

@Component(service=JobExecutor.class, property={
        JobExecutor.PROPERTY_TOPICS + "=" + CFRECS_PUSH_CF_MAPPINGS_TOPIC,
        JobExecutor.PROPERTY_TOPICS + "=" + CFRECS_PUSH_CF_TOPIC,
        JobExecutor.PROPERTY_TOPICS + "=" + CFRECS_DELETE_ENTITIES_TOPIC
})
public class CfRecsJobExecutor implements JobExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(CfRecsJobExecutor.class);

    public static final String CFRECS_PUSH_CF_MAPPINGS_TOPIC = "bullseye/cfrecs/push-cf-mappings";
    public static final String CFRECS_PUSH_CF_TOPIC = "bullseye/cfrecs/push-cf";
    public static final String CFRECS_DELETE_ENTITIES_TOPIC = "bullseye/cfrecs/delete-entities";
    public static final String PROP_MAPPING_PATHS = "mappingPaths";
    public static final String PROP_CF_PATHS = "cfPaths";
    public static final String PROP_ENTITY_IDS = "entityIds";

    @Reference
    private CfRecsMappingsService cfRecsMappingsService;

    @Reference
    private CfRecsEntitiesService cfRecsEntitiesService;


    public JobExecutionResult process(final Job job, JobExecutionContext context) {
        //process the job and return the result
        context.log("Job initialized");
        LOG.info("Job Initialized : bullseye/cfrecs/push-cf-mappings");

        if (CFRECS_PUSH_CF_MAPPINGS_TOPIC.equals(job.getTopic())) {
            String[] mappingPaths = job.getProperty(PROP_MAPPING_PATHS, String[].class);

            //initialize job progress with n number of steps
            context.initProgress(mappingPaths.length, -1);

            for (int i = 0; i < mappingPaths.length; i++) {
                    cfRecsMappingsService.pushMappingsToTarget(mappingPaths[i]);
                    context.incrementProgressCount(1);
                    //context.log("Step " + i + " complete.");
                    LOG.info("Step " + i + " complete.");
            }

            //stop processing if job was cancelled
            if (context.isStopped()) {
                context.log("Job Stopped.");
                return context.result().message("Job Stopped").cancelled();
            }

            //add job log
            context.log("Job Finished.");

            return context.result().message("Job Finished").succeeded();
        }

        if (CFRECS_PUSH_CF_TOPIC.equals(job.getTopic())) {
            final String[] cfPaths = job.getProperty(PROP_CF_PATHS, String[].class);

            context.initProgress(1, -1);

            cfRecsMappingsService.pushContentFragmentsToTarget(List.of(cfPaths));
            context.incrementProgressCount(1);

            return context.result().message("Job Finished").succeeded();
        }

        if (CFRECS_DELETE_ENTITIES_TOPIC.equals(job.getTopic())) {
            String[] entityIds = job.getProperty(PROP_ENTITY_IDS, String[].class);

            context.initProgress(1, -1);

            cfRecsEntitiesService.deleteEntities(List.of(entityIds));
            context.incrementProgressCount(1);

            return context.result().message("Job Finished").succeeded();
        }

        return null;
    }
}



