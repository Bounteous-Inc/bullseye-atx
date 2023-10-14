package com.bullseyeaem.core.antiflicker.commands;

import com.bullseyeaem.core.antiflicker.services.AntiFlickerService;
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
public class DeleteAntiFlickerConfigsCommand implements WCMCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteAntiFlickerConfigsCommand.class);

    @Reference
    private AntiFlickerService antiFlickerService;

    public String getCommandName() {
        return "deleteAntiFlickerConfigs";
    }

    public HtmlResponse performCommand(WCMCommandContext ctx, SlingHttpServletRequest request, SlingHttpServletResponse response, PageManager pageManager) {
        final HtmlResponse htmlResponse = new HtmlResponse();
        htmlResponse.setStatus(200, "Configs successfully deleted.");

        String[] configPaths = request.getParameterValues("path");
        try {
            antiFlickerService.deleteConfigs(configPaths);
        } catch (Exception e) {
            LOG.error("Error deleting flicker configs.", e);
            htmlResponse.setStatus(500, "Error deleting configs.");
        }

        return htmlResponse;
    }
}
