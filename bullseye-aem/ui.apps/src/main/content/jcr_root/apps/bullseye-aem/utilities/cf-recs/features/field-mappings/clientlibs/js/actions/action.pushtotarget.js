(function(window, document, Granite, $) {
    "use strict";

    const PUSH_TO_TARGET_URL= Granite.HTTP.externalize("/services/bullseye-aem/cf-recs/push-to-target");
    const PUSH_ALL_CONFIGS_TITLE = Granite.I18n.get("Push All");
    const PUSH_CONFIRMATION = Granite.I18n.get("Are you sure you would like to push the selected Content Fragments mappings to Target?");
    const PUSH_TEXT = Granite.I18n.get("Push");
    const CANCEL_TEXT = Granite.I18n.get("Cancel");
    const ERROR_TEXT = Granite.I18n.get("Error");

    const ui = $(window).adaptTo("foundation-ui");

    $(window).adaptTo("foundation-registry").register("foundation.collection.action.action", {
        name: "bullseye-aem.action.cf-recs.field-mappings.push-to-target",
        handler: function(name, el, config, collection, selections) {

            ui.prompt(
                PUSH_ALL_CONFIGS_TITLE,
                PUSH_CONFIRMATION,
                "notice",
                [
                    {
                        text: CANCEL_TEXT
                    },
                    {
                        text: PUSH_TEXT,
                        primary: true,
                        handler: function() {
                            const mappingPaths = selections.map(function(v) {
                                return $(v).data("foundationCollectionItemId");
                            });
                            publish(mappingPaths);
                        }
                    }
                ]
            );
        }
    });

    function publish(paths) {
        ui.wait();
        $.ajax({
            url: PUSH_TO_TARGET_URL,
            method: "POST",
            data: {
                "_charset_": "utf-8",
                "paths": paths,
            },
            success: function() {
                ui.notify(undefined, Granite.I18n.get("Content Fragments scheduled for push to Target."));
                //reload();
            },
            error: function(xhr) {
                ui.alert(
                    ERROR_TEXT,
                    Granite.I18n.get("Could not push Content Fragments to Target."),
                    'error');
            },
            complete: function() {
                ui.clearWait();
            }
        });
    }

})(window, document, Granite, Granite.$);
