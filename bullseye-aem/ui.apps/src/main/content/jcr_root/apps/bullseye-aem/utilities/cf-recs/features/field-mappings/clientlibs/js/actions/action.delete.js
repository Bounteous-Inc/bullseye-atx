(function(window, document, Granite, $) {
    "use strict";

    const WCM_COMMAND_URL= Granite.HTTP.externalize("/bin/wcmcommand");
    const DELETE_CONFIGS_TITLE = Granite.I18n.get("Delete Mapping");
    const DELETE_CONFIRMATION = Granite.I18n.get("Are you sure you would like to delete the selected mapping?");
    const DELETE_TEXT = Granite.I18n.get("Delete");
    const CANCEL_TEXT = Granite.I18n.get("Cancel");
    const ERROR_TEXT = Granite.I18n.get("Error");

    const ui = $(window).adaptTo("foundation-ui");

    $(window).adaptTo("foundation-registry").register("foundation.collection.action.action", {
        name: "bullseye-aem.action.cf-recs.mapping.delete",
        handler: function(name, el, config, collection, selections) {

            ui.prompt(
                DELETE_CONFIGS_TITLE,
                DELETE_CONFIRMATION,
                "notice",
                [
                    {
                        text: CANCEL_TEXT
                    },
                    {
                        text: DELETE_TEXT,
                        primary: true,
                        handler: function() {
                            const configPaths = selections.map(function(v) {
                                return $(v).data("foundationCollectionItemId");
                            });
                            deleteConfigs(configPaths);
                        }
                    }
                ]
            );

        }
    });

    function deleteConfigs(paths) {
        ui.wait();
        $.ajax({
            url: WCM_COMMAND_URL,
            method: "POST",
            data: {
                "_charset_": "utf-8",
                "path": paths,
                "cmd": "deleteCfRecsMapping"
            },
            success: function() {
                ui.notify(undefined, Granite.I18n.get("CF Recs mapping deleted."));
                reload();
            },
            error: function(xhr) {
                ui.alert(
                    ERROR_TEXT,
                    Granite.I18n.get("Could not delete CF Recs mappings."),
                    'error');
            },
            complete: function() {
                ui.clearWait();
            }
        });
    }

    function reload() {
        const contentApi = $(".foundation-content").adaptTo("foundation-content");
        if (contentApi) {
            // coral2
            contentApi.refresh();
        } else {
            // coral3
            const collectionAPI = $(".foundation-collection").adaptTo("foundation-collection");
            if (collectionAPI) {
                collectionAPI.reload();
            }
        }
    }

})(window, document, Granite, Granite.$);













