/*
 * jquery plugin for easier handling of "hijax" style url loading.
 *
 */

jQuery.ifhash = function(re, funcToInvoke) {
    if ( typeof jQuery.urlrewriteData == "undefined" ) {
        jQuery.urlrewriteData = {
            urls: new Array(),
            functions: new Array(),
            urlId: 0,
            listener: function(newLocation) {
                //log("listener");
                var matches = new Array();
                var matched = $.grep(jQuery.urlrewriteData.urls, function(re, i) {
                    //log("match #" + newLocation + " on " + re);
                    matches[i] = re.exec("#" + newLocation);
                    //log("res " + (matches[i] != null));
                    return (matches[i] != null);
                });
                //log("got " + matched.length);
                $.each(matched, function(i) {
                    //log("on " + i);
                    var func = jQuery.urlrewriteData.functions[i];
                    var match = matches[i];
                    match.shift();
                    //log("calling " + func + " match " + match.length);
                    func.apply(this, match);
                })
            }
        }

        //log("init");

        // todo: window.dhtmlHistory.create();
        dhtmlHistory.initialize();
        dhtmlHistory.addListener(jQuery.urlrewriteData.listener);
    }
    var id =  jQuery.urlrewriteData.urlId++;
    //log("add " + id);
    jQuery.urlrewriteData.urls[id] = re;
    jQuery.urlrewriteData.functions[id] = funcToInvoke;
    //log("done add");
};

jQuery.fn.ifhash = function(re, funcToInvoke) {
    jqContext = this;
    //log("shifted context");
    jQuery.ifhash.apply(this, [re, function() {
            //log("with context " + arguments + " " + jqContext);
            funcToInvoke.apply(jqContext, arguments);
        }]);
}

// usage example w/o jquery
/*todo: do automatically instantiate our history object*/
window.dhtmlHistory.create();




function log(msg) {
    $("#log").append("<br/>"+msg);
}

