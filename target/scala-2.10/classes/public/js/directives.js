'use strict';

/* Directives */


angular.module('junkMailApp.directives', [])
	.directive('appVersion', ['version',
		function(version) {
			return function(scope, elm, attrs) {
				elm.text(version);
			};
		}
	]);
