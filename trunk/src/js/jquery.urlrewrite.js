


function UrlRewrite() {
}

// todo: make local
var urlrewriteData = {
    urls: new Array(),
    functions: new Array(),
    urlId: 0
}


UrlRewrite.prototype = {
    add: function(fromRe, runFunction) {
        var id =  urlrewriteData.urlId++;
        log("add " + id);
        urlrewriteData.urls[id] = fromRe;
        urlrewriteData.functions[id] = runFunction;
    },

    listener: function(newLocation, historyData) {
        log("listener");
        var matches = new Array();
        var matched = $.grep(urlrewriteData.urls, function(re, i) {
            log("match #" + newLocation + " on " + re);
            matches[i] = re.exec("#" + newLocation);
            log("res " + (matches[i] != null));
            return (matches[i] != null);
        });
        log("got " + matched.length);
        $.each(matched, function(i) {
            log("on " + i);
            var func = urlrewriteData.functions[i];
            var match = matches[i];
            match.shift();
            log("calling " + func + " match " + match.length);
//            var groups = new Array();
//            for ( var j = 0; j < func.length; j++) {
//                groups[j] = match[j+1];
            //}
            log("about to call with params " + matches[i]);
            func.apply(this, matches[i]);
        })
    },

    init: function() {
        log("init");

        // todo: window.dhtmlHistory.create();
        dhtmlHistory.initialize();
        dhtmlHistory.addListener(this.listener);

    }
}

function log(msg) {
    $("#log").append("<br/>"+msg);
}



// jquery integration

jQuery.ifhash = function(re, funcToInvoke) {
    if ( typeof jQuery.urlrewrite == "undefined" ) {
        jQuery.urlrewrite = new UrlRewrite()
        jQuery.urlrewrite.init();
    }
    jQuery.urlrewrite.add(re, funcToInvoke);
    log("done add");
};

jQuery.fn.ifhash = function(re, funcToInvoke) {
    log("shifted context");
    jQuery.ifhash.apply(this, [re, function() {
            log("with context " + arguments + " " + funcToInvoke);
            funcToInvoke.apply(this, arguments);
        }]);
}
