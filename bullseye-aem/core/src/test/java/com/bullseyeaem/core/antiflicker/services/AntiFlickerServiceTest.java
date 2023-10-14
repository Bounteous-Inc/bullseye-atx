package com.bullseyeaem.core.antiflicker.services;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
public class AntiFlickerServiceTest {

    @BeforeEach
    void setup() {

    }

    @Test
    void getJavaScript(AemContext aemContext) {
        AntiFlickerService antiFlickerService = aemContext.getService(AntiFlickerService.class);

    }

    @Test
    void getAllConfigs(AemContext aemContext) {

    }

    @Test
    void deleteConfigs(AemContext aemContext) {

    }

    @Test
    void getConfigsForStagingIdentifier(AemContext aemContext) {

    }
}
