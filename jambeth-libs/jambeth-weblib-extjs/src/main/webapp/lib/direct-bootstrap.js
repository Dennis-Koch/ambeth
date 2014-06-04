/**
 * Will automatically load api-debug.js if any of these conditions is true: -
 * Current hostname is localhost - Current hostname is an IP v4 address -
 * Current protocol is "file:"
 * 
 * Will load api.js (minified) otherwise
 * 
 */
(function() {
	var localhostTests = [
			/^localhost$/,
			/\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:\d{1,5})?\b/ // IP
	// v4
	], host = window.location.hostname, queryString = window.location.search, test, i, ln, isDevelopment = null;

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

	// returns ExtDirect server resources
	// document.write('<script src="../api'+ (isDevelopment ? '-debug' : '')
	// +.js?group=form"></script>');
	document.write('<script src="api' + (isDevelopment ? '-debug' : '') + '.js"></script>');
})();