(function(window, document, Granite, $) {
    "use strict";

    const REPLICATION_URL= Granite.HTTP.externalize("/bin/replicate.json");
    const PUBLISH_ALL_CONFIGS_TITLE = Granite.I18n.get("Publish All");
    const PUBLISH_CONFIRMATION = Granite.I18n.get("Are you sure you would like to publish all anti-flicker configs and settings?");
    const PUBLISH_TEXT = Granite.I18n.get("Publish");
    const CANCEL_TEXT = Granite.I18n.get("Cancel");
    const ERROR_TEXT = Granite.I18n.get("Error");

    const ui = $(window).adaptTo("foundation-ui");

    $(window).adaptTo("foundation-registry").register("foundation.collection.action.action", {
        name: "bullseye-aem.action.antiflicker.publishall",
        handler: function(name, el, config, collection, selections) {
            const endpointPaths = ['/conf/global/settings/bullseye-aem/anti-flicker/configs', '/conf/global/settings/bullseye-aem/anti-flicker/settings'];

            ui.prompt(
                PUBLISH_ALL_CONFIGS_TITLE,
                PUBLISH_CONFIRMATION,
                "notice",
                [
                    {
                        text: CANCEL_TEXT
                    },
                    {
                        text: PUBLISH_TEXT,
                        primary: true,
                        handler: function() {
                            publish(endpointPaths);
                        }
                    }
                ]
            );

        }
    });

    function publish(paths) {
        ui.wait();
        $.ajax({
            url: REPLICATION_URL,
            method: "POST",
            data: {
                "_charset_": "utf-8",
                "path": paths,
                "cmd": "Activate"
            },
            success: function() {
                ui.notify(undefined, Granite.I18n.get("Anti-flicker configs scheduled for publishing."));
                //reload();
            },
            error: function(xhr) {
                ui.alert(
                    ERROR_TEXT,
                    Granite.I18n.get("Could not publish anti-flicker configs."),
                    'error');
            },
            complete: function() {
                ui.clearWait();
            }
        });
    }

})(window, document, Granite, Granite.$);
