/**
 * Will automatically load ext-all-dev.js if any of these conditions is true: -
 * Current hostname is localhost - Current hostname is an IP v4 address -
 * Current protocol is "file:"
 * 
 * Will load ext-all.js (minified) otherwise
 * 
 * Will also load ext-all-neptune.css
 */
(function() {
	var localhostTests = [
			/^localhost$/,
			/\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:\d{1,5})?\b/ // IP
	// v4
	], host = window.location.hostname, queryString = window.location.search, test, i, ln, isDevelopment = null, path;

	if (queryString.match('(\\?|&)debug') !== null) {
		isDevelopment = true;
	} else if (queryString.match('(\\?|&)nodebug') !== null) {
		isDevelopment = false;
	}

	if (isDevelopment === null) {
		for (i = 0, ln = localhostTests.length; i < ln; i++) {
			test = localhostTests[i];

			if (host.search(test) !== -1) {
				isDevelopment = true;
				break;
			}
		}
	}

	if (isDevelopment === null && window.location.protocol === 'file:') {
		isDevelopment = true;
	}
	var scripts = document.getElementsByTagName('script');
	var path = '';
    if (scripts && scripts.length>0) {
        for (var i in scripts) {
            if(scripts[i].src && scripts[i].src.match(/ext-bootstrap\.js$/)) {
                path = scripts[i].src.replace(/(.*)ext-bootstrap\.js$/, '$1');
            }
        }
    }

	path = path + 'extjs-4.2.x/';

	document.write('<script type="text/javascript" charset="UTF-8" src="' + path + 'ext-all'
			+ (isDevelopment ? '-dev' : '') + '.js"></script>');

	document.write('<link rel="stylesheet" type="text/css" href="' + path + 'resources/css/ext-all-neptune.css"/>');
})();
