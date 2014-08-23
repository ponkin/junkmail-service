'use strict';

/* Filters */

angular.module('junkMailApp.filters', []).
  filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    };
  }])
  .filter('toLocalTime', function(){
	  return function(time) {
	      return new Date(time).toLocaleString();
	    };
  })
  .filter('fromBase64', function(version) {	  
	  
    return function(text) {
		var decoded = '';
		try{			
			var atob = window.atob( text );
			var esc = escape(atob);
			decoded = decodeURIComponent(esc);
			console.log("Successfully decoded base64");
		}catch(err){
			console.log("Can not decode base64");
			decoded = text;
		}
      return decoded;
    };
  })
    .filter('reverse', function() {
        return function(items) {
            return items.slice().reverse();
        };
    });