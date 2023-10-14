package com.bullseyeaem.core.cfrecs.commands;

import com.bullseyeaem.core.cfrecs.services.CfRecsEntitiesService;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.commands.WCMCommand;
import com.day.cq.wcm.api.commands.WCMCommandContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HtmlResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component(
        service = WCMCommand.class
)
public class DeleteCfRecsEntityCommand implements WCMCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteCfRecsEntityCommand.class);

    @Reference
    private CfRecsEntitiesService cfRecsEntitiesService;

    @Override
    public String getCommandName() {
        return "deleteCfRecsEntity";
    }

    @Override
    public HtmlResponse performCommand(WCMCommandContext ctx, SlingHttpServletRequest request, SlingHttpServletResponse response, PageManager pageManager) {
        final HtmlResponse htmlResponse = new HtmlResponse();
        htmlResponse.setStatus(200, "Entities successfully deleted.");

        String[] entityIds = request.getParameterValues("ids");
        try {
            cfRecsEntitiesService.deleteEntities(List.of(entityIds));
        } catch (Exception e) {
            LOG.error("Error deleting Target entities.", e);
            htmlResponse.setStatus(500, "Error deleting entities.");
        }

        return htmlResponse;
    }
}
