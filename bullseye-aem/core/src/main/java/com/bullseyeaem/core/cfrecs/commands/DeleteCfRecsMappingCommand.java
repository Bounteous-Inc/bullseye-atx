package com.bullseyeaem.core.cfrecs.commands;

import com.bullseyeaem.core.cfrecs.services.CfRecsMappingsService;
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

@Component(
        service = WCMCommand.class
)
public class DeleteCfRecsMappingCommand implements WCMCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteCfRecsMappingCommand.class);

    @Reference
    private CfRecsMappingsService cfRecsMappingsService;

    public String getCommandName() {
        return "deleteCfRecsMapping";
    }

    public HtmlResponse performCommand(WCMCommandContext ctx, SlingHttpServletRequest request, SlingHttpServletResponse response, PageManager pageManager) {
        final HtmlResponse htmlResponse = new HtmlResponse();
        htmlResponse.setStatus(200, "Mapping successfully deleted.");

        String[] mappingPaths = request.getParameterValues("path");
        try {
            cfRecsMappingsService.deleteMappings(mappingPaths);
        } catch (Exception e) {
            LOG.error("Error deleting CF Recs mapping.", e);
            htmlResponse.setStatus(500, "Error deleting CF Recs mapping.");
        }

        return htmlResponse;
    }
}
