/**
 * jquery.urlrewrite.js
 *
 * A thin layer on top of reallysimplehistory http://code.google.com/p/reallysimplehistory/
 * that makes setting up "hijax" style listeners in jquery fairly easy. 
 *
 * This file is in three parts..
 *      1, slighly modified copy of
 *          http://www.json.org/json2.js
 *      2, copy of
 *          http://reallysimplehistory.googlecode.com/svn/trunk/rsh.js
 *      3, urlrewrite jquery functions
 *
 */



/***** start copy of http://www.json.org/json2.js *****/
/*
note: base JSON object renamed to urlrewriteJSON to avoid namespace issues
 */
/*
    http://www.JSON.org/json2.js
    2008-07-15

    Public Domain.

    NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.

    See http://www.JSON.org/js.html

    This file creates a global JSON object containing two methods: stringify
    and parse.

        JSON.stringify(value, replacer, space)
            value       any JavaScript value, usually an object or array.

            replacer    an optional parameter that determines how object
                        values are stringified for objects. It can be a
                        function or an array.

            space       an optional parameter that specifies the indentation
                        of nested structures. If it is omitted, the text will
                        be packed without extra whitespace. If it is a number,
                        it will specify the number of spaces to indent at each
                        level. If it is a string (such as '\t' or '&nbsp;'),
                        it contains the characters used to indent at each level.

            This method produces a JSON text from a JavaScript value.

            When an object value is found, if the object contains a toJSON
            method, its toJSON method will be called and the result will be
            stringified. A toJSON method does not serialize: it returns the
            value represented by the name/value pair that should be serialized,
            or undefined if nothing should be serialized. The toJSON method
            will be passed the key associated with the value, and this will be
            bound to the object holding the key.

            For example, this would serialize Dates as ISO strings.

                Date.prototype.toJSON = function (key) {
                    function f(n) {
                        // Format integers to have at least two digits.
                        return n < 10 ? '0' + n : n;
                    }

                    return this.getUTCFullYear()   + '-' +
                         f(this.getUTCMonth() + 1) + '-' +
                         f(this.getUTCDate())      + 'T' +
                         f(this.getUTCHours())     + ':' +
                         f(this.getUTCMinutes())   + ':' +
                         f(this.getUTCSeconds())   + 'Z';
                };

            You can provide an optional replacer method. It will be passed the
            key and value of each member, with this bound to the containing
            object. The value that is returned from your method will be
            serialized. If your method returns undefined, then the member will
            be excluded from the serialization.

            If the replacer parameter is an array, then it will be used to
            select the members to be serialized. It filters the results such
            that only members with keys listed in the replacer array are
            stringified.

            Values that do not have JSON representations, such as undefined or
            functions, will not be serialized. Such values in objects will be
            dropped; in arrays they will be replaced with null. You can use
            a replacer function to replace those with JSON values.
            JSON.stringify(undefined) returns undefined.

            The optional space parameter produces a stringification of the
            value that is filled with line breaks and indentation to make it
            easier to read.

            If the space parameter is a non-empty string, then that string will
            be used for indentation. If the space parameter is a number, then
            the indentation will be that many spaces.

            Example:

            text = JSON.stringify(['e', {pluribus: 'unum'}]);
            // text is '["e",{"pluribus":"unum"}]'


            text = JSON.stringify(['e', {pluribus: 'unum'}], null, '\t');
            // text is '[\n\t"e",\n\t{\n\t\t"pluribus": "unum"\n\t}\n]'

            text = JSON.stringify([new Date()], function (key, value) {
                return this[key] instanceof Date ?
                    'Date(' + this[key] + ')' : value;
            });
            // text is '["Date(---current time---)"]'


        JSON.parse(text, reviver)
            This method parses a JSON text to produce an object or array.
            It can throw a SyntaxError exception.

            The optional reviver parameter is a function that can filter and
            transform the results. It receives each of the keys and values,
            and its return value is used instead of the original value.
            If it returns what it received, then the structure is not modified.
            If it returns undefined then the member is deleted.

            Example:

            // Parse the text. Values that look like ISO date strings will
            // be converted to Date objects.

            myData = JSON.parse(text, function (key, value) {
                var a;
                if (typeof value === 'string') {
                    a =
/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/.exec(value);
                    if (a) {
                        return new Date(Date.UTC(+a[1], +a[2] - 1, +a[3], +a[4],
                            +a[5], +a[6]));
                    }
                }
                return value;
            });

            myData = JSON.parse('["Date(09/09/2001)"]', function (key, value) {
                var d;
                if (typeof value === 'string' &&
                        value.slice(0, 5) === 'Date(' &&
                        value.slice(-1) === ')') {
                    d = new Date(value.slice(5, -1));
                    if (d) {
                        return d;
                    }
                }
                return value;
            });


    This is a reference implementation. You are free to copy, modify, or
    redistribute.

    This code should be minified before deployment.
    See http://javascript.crockford.com/jsmin.html

    USE YOUR OWN COPY. IT IS EXTREMELY UNWISE TO LOAD CODE FROM SERVERS YOU DO
    NOT CONTROL.
*/

/*jslint evil: true */

/*global JSON */

/*members "", "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", call,
    charCodeAt, getUTCDate, getUTCFullYear, getUTCHours, getUTCMinutes,
    getUTCMonth, getUTCSeconds, hasOwnProperty, join, lastIndex, length,
    parse, propertyIsEnumerable, prototype, push, replace, slice, stringify,
    test, toJSON, toString
*/

if (!this.urlrewriteJSON) {

// Create a JSON object only if one does not already exist. We create the
// object in a closure to avoid creating global variables.

    urlrewriteJSON = function () {

        function f(n) {
            // Format integers to have at least two digits.
            return n < 10 ? '0' + n : n;
        }

        Date.prototype.toJSON = function (key) {

            return this.getUTCFullYear()   + '-' +
                 f(this.getUTCMonth() + 1) + '-' +
                 f(this.getUTCDate())      + 'T' +
                 f(this.getUTCHours())     + ':' +
                 f(this.getUTCMinutes())   + ':' +
                 f(this.getUTCSeconds())   + 'Z';
        };

        String.prototype.toJSON =
        Number.prototype.toJSON =
        Boolean.prototype.toJSON = function (key) {
            return this.valueOf();
        };

        var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
            escapeable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
            gap,
            indent,
            meta = {    // table of character substitutions
                '\b': '\\b',
                '\t': '\\t',
                '\n': '\\n',
                '\f': '\\f',
                '\r': '\\r',
                '"' : '\\"',
                '\\': '\\\\'
            },
            rep;


        function quote(string) {

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe escape
// sequences.

            escapeable.lastIndex = 0;
            return escapeable.test(string) ?
                '"' + string.replace(escapeable, function (a) {
                    var c = meta[a];
                    if (typeof c === 'string') {
                        return c;
                    }
                    return '\\u' + ('0000' +
                            (+(a.charCodeAt(0))).toString(16)).slice(-4);
                }) + '"' :
                '"' + string + '"';
        }


        function str(key, holder) {

// Produce a string from holder[key].

            var i,          // The loop counter.
                k,          // The member key.
                v,          // The member value.
                length,
                mind = gap,
                partial,
                value = holder[key];

// If the value has a toJSON method, call it to obtain a replacement value.

            if (value && typeof value === 'object' &&
                    typeof value.toJSON === 'function') {
                value = value.toJSON(key);
            }

// If we were called with a replacer function, then call the replacer to
// obtain a replacement value.

            if (typeof rep === 'function') {
                value = rep.call(holder, key, value);
            }

// What happens next depends on the value's type.

            switch (typeof value) {
            case 'string':
                return quote(value);

            case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

                return isFinite(value) ? String(value) : 'null';

            case 'boolean':
            case 'null':

// If the value is a boolean or null, convert it to a string. Note:
// typeof null does not produce 'null'. The case is included here in
// the remote chance that this gets fixed someday.

                return String(value);

// If the type is 'object', we might be dealing with an object or an array or
// null.

            case 'object':

// Due to a specification blunder in ECMAScript, typeof null is 'object',
// so watch out for that case.

                if (!value) {
                    return 'null';
                }

// Make an array to hold the partial results of stringifying this object value.

                gap += indent;
                partial = [];

// If the object has a dontEnum length property, we'll treat it as an array.

                if (typeof value.length === 'number' &&
                        !(value.propertyIsEnumerable('length'))) {

// The object is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                    length = value.length;
                    for (i = 0; i < length; i += 1) {
                        partial[i] = str(i, value) || 'null';
                    }

// Join all of the elements together, separated with commas, and wrap them in
// brackets.

                    v = partial.length === 0 ? '[]' :
                        gap ? '[\n' + gap +
                                partial.join(',\n' + gap) + '\n' +
                                    mind + ']' :
                              '[' + partial.join(',') + ']';
                    gap = mind;
                    return v;
                }

// If the replacer is an array, use it to select the members to be stringified.

                if (rep && typeof rep === 'object') {
                    length = rep.length;
                    for (i = 0; i < length; i += 1) {
                        k = rep[i];
                        if (typeof k === 'string') {
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                } else {

// Otherwise, iterate through all of the keys in the object.

                    for (k in value) {
                        if (Object.hasOwnProperty.call(value, k)) {
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                }

// Join all of the member texts together, separated with commas,
// and wrap them in braces.

                v = partial.length === 0 ? '{}' :
                    gap ? '{\n' + gap + partial.join(',\n' + gap) + '\n' +
                            mind + '}' : '{' + partial.join(',') + '}';
                gap = mind;
                return v;
            }
        }

// Return the JSON object containing the stringify and parse methods.

        return {
            stringify: function (value, replacer, space) {

// The stringify method takes a value and an optional replacer, and an optional
// space parameter, and returns a JSON text. The replacer can be a function
// that can replace values, or an array of strings that will select the keys.
// A default replacer method can be provided. Use of the space parameter can
// produce text that is more easily readable.

                var i;
                gap = '';
                indent = '';

// If the space parameter is a number, make an indent string containing that
// many spaces.

                if (typeof space === 'number') {
                    for (i = 0; i < space; i += 1) {
                        indent += ' ';
                    }

// If the space parameter is a string, it will be used as the indent string.

                } else if (typeof space === 'string') {
                    indent = space;
                }

// If there is a replacer, it must be a function or an array.
// Otherwise, throw an error.

                rep = replacer;
                if (replacer && typeof replacer !== 'function' &&
                        (typeof replacer !== 'object' ||
                         typeof replacer.length !== 'number')) {
                    throw new Error('JSON.stringify');
                }

// Make a fake root object containing our value under the key of ''.
// Return the result of stringifying the value.

                return str('', {'': value});
            },


            parse: function (text, reviver) {

// The parse method takes a text and an optional reviver function, and returns
// a JavaScript value if the text is a valid JSON text.

                var j;

                function walk(holder, key) {

// The walk method is used to recursively walk the resulting structure so
// that modifications can be made.

                    var k, v, value = holder[key];
                    if (value && typeof value === 'object') {
                        for (k in value) {
                            if (Object.hasOwnProperty.call(value, k)) {
                                v = walk(value, k);
                                if (v !== undefined) {
                                    value[k] = v;
                                } else {
                                    delete value[k];
                                }
                            }
                        }
                    }
                    return reviver.call(holder, key, value);
                }


// Parsing happens in four stages. In the first stage, we replace certain
// Unicode characters with escape sequences. JavaScript handles many characters
// incorrectly, either silently deleting them, or treating them as line endings.

                cx.lastIndex = 0;
                if (cx.test(text)) {
                    text = text.replace(cx, function (a) {
                        return '\\u' + ('0000' +
                                (+(a.charCodeAt(0))).toString(16)).slice(-4);
                    });
                }

// In the second stage, we run the text against regular expressions that look
// for non-JSON patterns. We are especially concerned with '()' and 'new'
// because they can cause invocation, and '=' because it can cause mutation.
// But just to be safe, we want to reject all unexpected forms.

// We split the second stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

                if (/^[\],:{}\s]*$/.
test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@').
replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the third stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                    j = eval('(' + text + ')');

// In the optional fourth stage, we recursively walk the new structure, passing
// each name/value pair to a reviver function for possible transformation.

                    return typeof reviver === 'function' ?
                        walk({'': j}, '') : j;
                }

// If the text is not JSON parseable, then a SyntaxError is thrown.

                throw new SyntaxError('JSON.parse');
            }
        };
    }();
}

/***** end copy of http://www.json.org/json2.js *****/









/***** start copy of http://reallysimplehistory.googlecode.com/svn/trunk/rsh.js *****/
/*
Copyright (c) 2007 Brian Dillard and Brad Neuberg:
Brian Dillard | Project Lead | bdillard@pathf.com | http://blogs.pathf.com/agileajax/
Brad Neuberg | Original Project Creator | http://codinginparadise.org

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/*
	dhtmlHistory: An object that provides history, history data, and bookmarking for DHTML and Ajax applications.

	dependencies:
		* the historyStorage object included in this file.

*/
window.dhtmlHistory = {

	/*Public: User-agent booleans*/
	isIE: false,
	isOpera: false,
	isSafari: false,
	isKonquerer: false,
	isGecko: false,
	isSupported: false,

	/*Public: Create the DHTML history infrastructure*/
	create: function(options) {

		/*
			options - object to store initialization parameters
			options.debugMode - boolean that causes hidden form fields to be shown for development purposes.
			options.toJSON - function to override default JSON stringifier
			options.fromJSON - function to override default JSON parser
		*/

		var that = this;

		/*set user-agent flags*/
		var UA = navigator.userAgent.toLowerCase();
		var platform = navigator.platform.toLowerCase();
		var vendor = navigator.vendor || "";
		if (vendor === "KDE") {
			this.isKonqueror = true;
			this.isSupported = false;
		} else if (typeof window.opera !== "undefined") {
			this.isOpera = true;
			this.isSupported = true;
		} else if (typeof document.all !== "undefined") {
			this.isIE = true;
			this.isSupported = true;
		} else if (vendor.indexOf("Apple Computer, Inc.") > -1) {
			this.isSafari = true;
			this.isSupported = (platform.indexOf("mac") > -1);
		} else if (UA.indexOf("gecko") != -1) {
			this.isGecko = true;
			this.isSupported = true;
		}

		/*Set up the historyStorage object; pass in init parameters*/
		window.historyStorage.setup(options);

		/*Execute browser-specific setup methods*/
		if (this.isSafari) {
			this.createSafari();
		} else if (this.isOpera) {
			this.createOpera();
		}

		/*Get our initial location*/
		var initialHash = this.getCurrentLocation();

		/*Save it as our current location*/
		this.currentLocation = initialHash;

		/*Now that we have a hash, create IE-specific code*/
		if (this.isIE) {
			this.createIE(initialHash);
		}

		/*Add an unload listener for the page; this is needed for FF 1.5+ because this browser caches all dynamic updates to the
		page, which can break some of our logic related to testing whether this is the first instance a page has loaded or whether
		it is being pulled from the cache*/

		var unloadHandler = function() {
			that.firstLoad = null;
		};

		this.addEventListener(window,'unload',unloadHandler);

		/*Determine if this is our first page load; for IE, we do this in this.iframeLoaded(), which is fired on pageload. We do it
		there because we have no historyStorage at this point, which only exists after the page is finished loading in IE*/
		if (this.isIE) {
			/*The iframe will get loaded on page load, and we want to ignore this fact*/
			this.ignoreLocationChange = true;
		} else {
			if (!historyStorage.hasKey(this.PAGELOADEDSTRING)) {
				/*This is our first page load, so ignore the location change and add our special history entry*/
				this.ignoreLocationChange = true;
				this.firstLoad = true;
				historyStorage.put(this.PAGELOADEDSTRING, true);
			} else {
				/*This isn't our first page load, so indicate that we want to pay attention to this location change*/
				this.ignoreLocationChange = false;
				/*For browsers other than IE, fire a history change event; on IE, the event will be thrown automatically when its
				hidden iframe reloads on page load. Unfortunately, we don't have any listeners yet; indicate that we want to fire
				an event when a listener is added.*/
				this.fireOnNewListener = true;
			}
		}

		/*Other browsers can use a location handler that checks at regular intervals as their primary mechanism; we use it for IE as
		well to handle an important edge case; see checkLocation() for details*/
		var locationHandler = function() {
			that.checkLocation();
		};
		setInterval(locationHandler, 100);
	},

	/*Public: Initialize our DHTML history. You must call this after the page is finished loading.*/
	initialize: function() {
		/*IE needs to be explicitly initialized. IE doesn't autofill form data until the page is finished loading, so we have to wait*/
		if (this.isIE) {
			/*If this is the first time this page has loaded*/
			if (!historyStorage.hasKey(this.PAGELOADEDSTRING)) {
				/*For IE, we do this in initialize(); for other browsers, we do it in create()*/
				this.fireOnNewListener = false;
				this.firstLoad = true;
				historyStorage.put(this.PAGELOADEDSTRING, true);
			}
			/*Else if this is a fake onload event*/
			else {
				this.fireOnNewListener = true;
				this.firstLoad = false;
			}
		}
	},

	/*Public: Adds a history change listener. Note that only one listener is supported at this time.*/
	addListener: function(listener) {
		this.listener = listener;
		/*If the page was just loaded and we should not ignore it, fire an event to our new listener now*/
		if (this.fireOnNewListener) {
			this.fireHistoryEvent(this.currentLocation);
			this.fireOnNewListener = false;
		}
	},

	/*Public: Generic utility function for attaching events*/
	addEventListener: function(o,e,l) {
		if (o.addEventListener) {
			o.addEventListener(e,l,false);
		} else if (o.attachEvent) {
			o.attachEvent('on'+e,function() {
				l(window.event);
			});
		}
	},

	/*Public: Add a history point.*/
	add: function(newLocation, historyData) {

		if (this.isSafari) {

			/*Remove any leading hash symbols on newLocation*/
			newLocation = this.removeHash(newLocation);

			/*Store the history data into history storage*/
			historyStorage.put(newLocation, historyData);

			/*Save this as our current location*/
			this.currentLocation = newLocation;

			/*Change the browser location*/
			window.location.hash = newLocation;

			/*Save this to the Safari form field*/
			this.putSafariState(newLocation);

		} else {

			/*Most browsers require that we wait a certain amount of time before changing the location, such
			as 200 MS; rather than forcing external callers to use window.setTimeout to account for this,
			we internally handle it by putting requests in a queue.*/
			var that = this;
			var addImpl = function() {

				/*Indicate that the current wait time is now less*/
				if (that.currentWaitTime > 0) {
					that.currentWaitTime = that.currentWaitTime - that.waitTime;
				}

				/*Remove any leading hash symbols on newLocation*/
				newLocation = that.removeHash(newLocation);

				/*IE has a strange bug; if the newLocation is the same as _any_ preexisting id in the
				document, then the history action gets recorded twice; throw a programmer exception if
				there is an element with this ID*/
				if (document.getElementById(newLocation) && that.debugMode) {
					var e = "Exception: History locations can not have the same value as _any_ IDs that might be in the document,"
					+ " due to a bug in IE; please ask the developer to choose a history location that does not match any HTML"
					+ " IDs in this document. The following ID is already taken and cannot be a location: " + newLocation;
					throw new Error(e);
				}

				/*Store the history data into history storage*/
				historyStorage.put(newLocation, historyData);

				/*Indicate to the browser to ignore this upcomming location change since we're making it programmatically*/
				that.ignoreLocationChange = true;

				/*Indicate to IE that this is an atomic location change block*/
				that.ieAtomicLocationChange = true;

				/*Save this as our current location*/
				that.currentLocation = newLocation;

				/*Change the browser location*/
				window.location.hash = newLocation;

				/*Change the hidden iframe's location if on IE*/
				if (that.isIE) {
					that.iframe.src = "blank.html?" + newLocation;
				}

				/*End of atomic location change block for IE*/
				that.ieAtomicLocationChange = false;
			};

			/*Now queue up this add request*/
			window.setTimeout(addImpl, this.currentWaitTime);

			/*Indicate that the next request will have to wait for awhile*/
			this.currentWaitTime = this.currentWaitTime + this.waitTime;
		}
	},

	/*Public*/
	isFirstLoad: function() {
		return this.firstLoad;
	},

	/*Public*/
	getVersion: function() {
		return "0.6";
	},

	/*Get browser's current hash location; for Safari, read value from a hidden form field*/

	/*Public*/
	getCurrentLocation: function() {
		var r = (this.isSafari
			? this.getSafariState()
			: this.getCurrentHash()
		);
		return r;
	},

	/*Public: Manually parse the current url for a hash; tip of the hat to YUI*/
    getCurrentHash: function() {
		var r = window.location.href;
		var i = r.indexOf("#");
		return (i >= 0
			? r.substr(i+1)
			: ""
		);
    },

	/*- - - - - - - - - - - -*/

	/*Private: Constant for our own internal history event called when the page is loaded*/
	PAGELOADEDSTRING: "DhtmlHistory_pageLoaded",

	/*Private: Our history change listener.*/
	listener: null,

	/*Private: MS to wait between add requests - will be reset for certain browsers*/
	waitTime: 200,

	/*Private: MS before an add request can execute*/
	currentWaitTime: 0,

	/*Private: Our current hash location, without the "#" symbol.*/
	currentLocation: null,

	/*Private: Hidden iframe used to IE to detect history changes*/
	iframe: null,

	/*Private: Flags and DOM references used only by Safari*/
	safariHistoryStartPoint: null,
	safariStack: null,
	safariLength: null,

	/*Private: Flag used to keep checkLocation() from doing anything when it discovers location changes we've made ourselves
	programmatically with the add() method. Basically, add() sets this to true. When checkLocation() discovers it's true,
	it refrains from firing our listener, then resets the flag to false for next cycle. That way, our listener only gets fired on
	history change events triggered by the user via back/forward buttons and manual hash changes. This flag also helps us set up
	IE's special iframe-based method of handling history changes.*/
	ignoreLocationChange: null,

	/*Private: A flag that indicates that we should fire a history change event when we are ready, i.e. after we are initialized and
	we have a history change listener. This is needed due to an edge case in browsers other than IE; if you leave a page entirely
	then return, we must fire this as a history change event. Unfortunately, we have lost all references to listeners from earlier,
	because JavaScript clears out.*/
	fireOnNewListener: null,

	/*Private: A variable that indicates whether this is the first time this page has been loaded. If you go to a web page, leave it
	for another one, and then return, the page's onload listener fires again. We need a way to differentiate between the first page
	load and subsequent ones. This variable works hand in hand with the pageLoaded variable we store into historyStorage.*/
	firstLoad: null,

	/*Private: A variable to handle an important edge case in IE. In IE, if a user manually types an address into their browser's
	location bar, we must intercept this by calling checkLocation() at regular intervals. However, if we are programmatically
	changing the location bar ourselves using the add() method, we need to ignore these changes in checkLocation(). Unfortunately,
	these changes take several lines of code to complete, so for the duration of those lines of code, we set this variable to true.
	That signals to checkLocation() to ignore the change-in-progress. Once we're done with our chunk of location-change code in
	add(), we set this back to false. We'll do the same thing when capturing user-entered address changes in checkLocation itself.*/
	ieAtomicLocationChange: null,

	/*Private: Create IE-specific DOM nodes and overrides*/
	createIE: function(initialHash) {
		/*write out a hidden iframe for IE and set the amount of time to wait between add() requests*/
		this.waitTime = 400;/*IE needs longer between history updates*/
		var styles = (historyStorage.debugMode
			? 'width: 800px;height:80px;border:1px solid black;'
			: historyStorage.hideStyles
		);
		var iframeID = "rshHistoryFrame";
		var iframeHTML = '<iframe frameborder="0" id="' + iframeID + '" style="' + styles + '" src="blank.html?' + initialHash + '"></iframe>';
		document.write(iframeHTML);
		this.iframe = document.getElementById(iframeID);
	},

	/*Private: Create Opera-specific DOM nodes and overrides*/
	createOpera: function() {
		this.waitTime = 400;/*Opera needs longer between history updates*/
		var imgHTML = '<img src="javascript:location.href=\'javascript:dhtmlHistory.checkLocation();\';" style="' + historyStorage.hideStyles + '" />';
		document.write(imgHTML);
	},

	/*Private: Create Safari-specific DOM nodes and overrides*/
	createSafari: function() {
		var formID = "rshSafariForm";
		var stackID = "rshSafariStack";
		var lengthID = "rshSafariLength";
		var formStyles = historyStorage.debugMode ? historyStorage.showStyles : historyStorage.hideStyles;
		var inputStyles = (historyStorage.debugMode
			? 'width:800px;height:20px;border:1px solid black;margin:0;padding:0;'
			: historyStorage.hideStyles
		);
		var safariHTML = '<form id="' + formID + '" style="' + formStyles + '">'
			+ '<input type="text" style="' + inputStyles + '" id="' + stackID + '" value="[]"/>'
			+ '<input type="text" style="' + inputStyles + '" id="' + lengthID + '" value=""/>'
		+ '</form>';
		document.write(safariHTML);
		this.safariStack = document.getElementById(stackID);
		this.safariLength = document.getElementById(lengthID);
		if (!historyStorage.hasKey(this.PAGELOADEDSTRING)) {
			this.safariHistoryStartPoint = history.length;
			this.safariLength.value = this.safariHistoryStartPoint;
		} else {
			this.safariHistoryStartPoint = this.safariLength.value;
		}
	},

	/*Private: Safari method to read the history stack from a hidden form field*/
	getSafariStack: function() {
		var r = this.safariStack.value;
		return historyStorage.fromJSON(r);
	},

	/*Private: Safari method to read from the history stack*/
	getSafariState: function() {
		var stack = this.getSafariStack();
		var state = stack[history.length - this.safariHistoryStartPoint - 1];
		return state;
	},
	/*Private: Safari method to write the history stack to a hidden form field*/
	putSafariState: function(newLocation) {
	    var stack = this.getSafariStack();
	    stack[history.length - this.safariHistoryStartPoint] = newLocation;
	    this.safariStack.value = historyStorage.toJSON(stack);
	},

	/*Private: Notify the listener of new history changes.*/
	fireHistoryEvent: function(newHash) {
		/*extract the value from our history storage for this hash*/
		var historyData = historyStorage.get(newHash);
		/*call our listener*/
		this.listener.call(null, newHash, historyData);
	},

	/*Private: See if the browser has changed location. This is the primary history mechanism for Firefox. For IE, we use this to
	handle an important edge case: if a user manually types in a new hash value into their IE location bar and press enter, we want to
	to intercept this and notify any history listener.*/
	checkLocation: function() {

		/*Ignore any location changes that we made ourselves for browsers other than IE*/
		if (!this.isIE && this.ignoreLocationChange) {
			this.ignoreLocationChange = false;
			return;
		}

		/*If we are dealing with IE and we are in the middle of making a location change from an iframe, ignore it*/
		if (!this.isIE && this.ieAtomicLocationChange) {
			return;
		}

		/*Get hash location*/
		var hash = this.getCurrentLocation();

		/*Do nothing if there's been no change*/
		if (hash == this.currentLocation) {
			return;
		}

		/*In IE, users manually entering locations into the browser; we do this by comparing the browser's location against the
		iframe's location; if they differ, we are dealing with a manual event and need to place it inside our history, otherwise
		we can return*/
		this.ieAtomicLocationChange = true;

		if (this.isIE && this.getIframeHash() != hash) {
			this.iframe.src = "blank.html?" + hash;
		}
		else if (this.isIE) {
			/*the iframe is unchanged*/
			return;
		}

		/*Save this new location*/
		this.currentLocation = hash;

		this.ieAtomicLocationChange = false;

		/*Notify listeners of the change*/
		this.fireHistoryEvent(hash);
	},

	/*Private: Get the current location of IE's hidden iframe.*/
	getIframeHash: function() {
		var doc = this.iframe.contentWindow.document;
		var hash = String(doc.location.search);
		if (hash.length == 1 && hash.charAt(0) == "?") {
			hash = "";
		}
		else if (hash.length >= 2 && hash.charAt(0) == "?") {
			hash = hash.substring(1);
		}
		return hash;
	},

	/*Private: Remove any leading hash that might be on a location.*/
	removeHash: function(hashValue) {
		var r;
		if (hashValue === null || hashValue === undefined) {
			r = null;
		}
		else if (hashValue === "") {
			r = "";
		}
		else if (hashValue.length == 1 && hashValue.charAt(0) == "#") {
			r = "";
		}
		else if (hashValue.length > 1 && hashValue.charAt(0) == "#") {
			r = hashValue.substring(1);
		}
		else {
			r = hashValue;
		}
		return r;
	},

	/*Private: For IE, tell when the hidden iframe has finished loading.*/
	iframeLoaded: function(newLocation) {
		/*ignore any location changes that we made ourselves*/
		if (this.ignoreLocationChange) {
			this.ignoreLocationChange = false;
			return;
		}

		/*Get the new location*/
		var hash = String(newLocation.search);
		if (hash.length == 1 && hash.charAt(0) == "?") {
			hash = "";
		}
		else if (hash.length >= 2 && hash.charAt(0) == "?") {
			hash = hash.substring(1);
		}
		/*Keep the browser location bar in sync with the iframe hash*/
		window.location.hash = hash;

		/*Notify listeners of the change*/
		this.fireHistoryEvent(hash);
	}

};

/*
	historyStorage: An object that uses a hidden form to store history state across page loads. The mechanism for doing so relies on
	the fact that browsers save the text in form data for the life of the browser session, which means the text is still there when
	the user navigates back to the page. This object can be used independently of the dhtmlHistory object for caching of Ajax
	session information.

	dependencies:
		* json2007.js (included in a separate file) or alternate JSON methods passed in through an options bundle.
*/
window.historyStorage = {

	/*Public: Set up our historyStorage object for use by dhtmlHistory or other objects*/
	setup: function(options) {

		/*
			options - object to store initialization parameters - passed in from dhtmlHistory or directly into historyStorage
			options.debugMode - boolean that causes hidden form fields to be shown for development purposes.
			options.toJSON - function to override default JSON stringifier
			options.fromJSON - function to override default JSON parser
		*/

		/*process init parameters*/
		if (typeof options !== "undefined") {
			if (options.debugMode) {
				this.debugMode = options.debugMode;
			}
			if (options.toJSON) {
				this.toJSON = options.toJSON;
			}
			if (options.fromJSON) {
				this.fromJSON = options.fromJSON;
			}
		}

		/*write a hidden form and textarea into the page; we'll stow our history stack here*/
		var formID = "rshStorageForm";
		var textareaID = "rshStorageField";
		var formStyles = this.debugMode ? historyStorage.showStyles : historyStorage.hideStyles;
		var textareaStyles = (historyStorage.debugMode
			? 'width: 800px;height:80px;border:1px solid black;'
			: historyStorage.hideStyles
		);
		var textareaHTML = '<form id="' + formID + '" style="' + formStyles + '">'
			+ '<textarea id="' + textareaID + '" style="' + textareaStyles + '"></textarea>'
		+ '</form>';
		document.write(textareaHTML);
		this.storageField = document.getElementById(textareaID);
		if (typeof window.opera !== "undefined") {
			this.storageField.focus();/*Opera needs to focus this element before persisting values in it*/
		}
	},

	/*Public*/
	put: function(key, value) {
		this.assertValidKey(key);
		/*if we already have a value for this, remove the value before adding the new one*/
		if (this.hasKey(key)) {
			this.remove(key);
		}
		/*store this new key*/
		this.storageHash[key] = value;
		/*save and serialize the hashtable into the form*/
		this.saveHashTable();
	},

	/*Public*/
	get: function(key) {
		this.assertValidKey(key);
		/*make sure the hash table has been loaded from the form*/
		this.loadHashTable();
		var value = this.storageHash[key];
		if (value === undefined) {
			value = null;
		}
		return value;
	},

	/*Public*/
	remove: function(key) {
		this.assertValidKey(key);
		/*make sure the hash table has been loaded from the form*/
		this.loadHashTable();
		/*delete the value*/
		delete this.storageHash[key];
		/*serialize and save the hash table into the form*/
		this.saveHashTable();
	},

	/*Public: Clears out all saved data.*/
	reset: function() {
		this.storageField.value = "";
		this.storageHash = {};
	},

	/*Public*/
	hasKey: function(key) {
		this.assertValidKey(key);
		/*make sure the hash table has been loaded from the form*/
		this.loadHashTable();
		return (typeof this.storageHash[key] !== "undefined");
	},

	/*Public*/
	isValidKey: function(key) {
		return (typeof key === "string");
	},

	/*Public - CSS strings utilized by both objects to hide or show behind-the-scenes DOM elements*/
	showStyles: 'border:0;margin:0;padding:0;',
	hideStyles: 'left:-1000px;top:-1000px;width:1px;height:1px;border:0;position:absolute;',

	/*Public - debug mode flag*/
	debugMode: false,

	/*- - - - - - - - - - - -*/

	/*Private: Our hash of key name/values.*/
	storageHash: {},

	/*Private: If true, we have loaded our hash table out of the storage form.*/
	hashLoaded: false,

	/*Private: DOM reference to our history field*/
	storageField: null,

	/*Private: Assert that a key is valid; throw an exception if it not.*/
	assertValidKey: function(key) {
		var isValid = this.isValidKey(key);
		if (!isValid && this.debugMode) {
			throw new Error("Please provide a valid key for window.historyStorage. Invalid key = " + key + ".");
		}
	},

	/*Private: Load the hash table up from the form.*/
	loadHashTable: function() {
		if (!this.hashLoaded) {
			var serializedHashTable = this.storageField.value;
			if (serializedHashTable !== "" && serializedHashTable !== null) {
				this.storageHash = this.fromJSON(serializedHashTable);
				this.hashLoaded = true;
			}
		}
	},
	/*Private: Save the hash table into the form.*/
	saveHashTable: function() {
		this.loadHashTable();
		var serializedHashTable = this.toJSON(this.storageHash);
		this.storageField.value = serializedHashTable;
	},
	/*Private: Bridges for our JSON implementations - both rely on 2007 JSON.org library - can be overridden by options bundle*/
	toJSON: function(o) {
		return o.toJSONString();
	},
	fromJSON: function(s) {
		return s.parseJSON();
	}
};
/***** end copy of http://reallysimplehistory.googlecode.com/svn/trunk/rsh.js *****/














/*
 * jquery plugin for easier handling of "hijax" style url loading.
 *
 * @Author Paul Tuckey
 *
 * Public Domain.
 *
 * NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.
 *
 */

/**
 * Allows all hyperlinks in a page to be rewritten if the href matches the specified regularexpression.
 *
 * @param reFrom regular expression to match hrefs against
 * @param to replacement string for the href
 */
jQuery.urlrewrite = function(reFrom, to) {
    //console.log("on " + reFrom);
    jQuery("a[href]").each(function() {
        var href = jQuery(this).attr("href");
        var match = reFrom.exec(href);
        //console.log("got " + reFrom + " " + href + " " + match);
        if ( match ) {
            jQuery(this).attr("href", href.replace(reFrom, to));
        }
    });
}

/**
 * Allows a function to be called if a particular url is matched.
 *
 * @param re regular expression to use for matching urls
 * @param funcToInvoke the function to invoke if the url is matched
 */
jQuery.urllisten = function(re, funcToInvoke) {
    // initialise the data obj for urlrewrite data
    if ( typeof jQuery.urlrewriteData != "object" ) {
        jQuery.urlrewriteData = {
            urls: new Array(),
            functions: new Array(),
            nextId: 0
        }
        dhtmlHistory.initialize();
        // this function is invoked by dhtmlHistory if hash is changed
        dhtmlHistory.addListener(function(newLocation) {
            var matches = new Array();
            var matched = $.grep(jQuery.urlrewriteData.urls, function(re, i) {
                matches[i] = re.exec("#" + newLocation);
                return (matches[i] != null);
            });
            $.each(matched, function(i) {
                var func = jQuery.urlrewriteData.functions[i];
                var match = matches[i];
                match.shift();
                func.apply(this, match);
            });
        });
    }

    // populate this rule into the arrays
    var id = jQuery.urlrewriteData.nextId++;
    jQuery.urlrewriteData.urls[id] = re;
    jQuery.urlrewriteData.functions[id] = funcToInvoke;

    // are we currently on this url? invoke function if we are
    if ( document.location.hash ) {
        var match = re.exec("#" + document.location.hash);
        if ( match ) {
            match.shift();
            funcToInvoke.apply(this, match);
        }
    }

};

/**
 *
 * @param re regular expression to use for matching urls
 * @param funcToInvoke the function to invoke if the url is matched
 */
jQuery.fn.urllisten = function(re, funcToInvoke) {
    var jqContext = this;
    jQuery.urllisten.apply(this, [re, function() {
            funcToInvoke.apply(jqContext, arguments);
        }]);
}

// instantiate our history object
window.dhtmlHistory.create({
    toJSON: function(o) {
        return urlrewriteJSON.stringify(o);
    } , fromJSON: function(s) {
        return urlrewriteJSON.parse(s);
    }} );
