package com.bullseyeaem.core.antiflicker.servlets;

import com.bullseyeaem.core.antiflicker.services.AntiFlickerService;
import com.bullseyeaem.core.common.exceptions.BullseyeException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;

@Component(service = {Servlet.class},
    property = {
        SERVICE_DESCRIPTION + "=" + "Bullseye AEM - Anti-Flicker Servlet",
        SLING_SERVLET_METHODS + "=" + "[" + METHOD_GET + "]",
        SLING_SERVLET_PATHS + "=" + "/services/flicker",
        SLING_SERVLET_EXTENSIONS + "=" + "js"
    })
public class AntiFlickerServlet extends SlingSafeMethodsServlet {
    private static final long serialVersionUID = -7621747297747967924L;
    private static final Logger log = LoggerFactory.getLogger(AntiFlickerServlet.class);

    @Reference
    private AntiFlickerService antiFlickerService;

    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws IOException {

        response.setContentType("application/javascript");

        try {
            final String[] selectorsArray = request.getRequestPathInfo().getSelectors();
            final boolean minify = Arrays.stream(selectorsArray).anyMatch("min"::equalsIgnoreCase);
            response.getWriter().write(antiFlickerService.getJavaScript(selectorsArray.length > 0 ? selectorsArray[0] : EMPTY, minify));
        } catch (BullseyeException e) {
            log.error("Error getting anti-flicker script.", e);
            response.setStatus(500);
        }
    }
}

