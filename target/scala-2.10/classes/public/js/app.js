'use strict';


angular.module('junkMailApp', [
    'ngSanitize',
    'ngRoute',
    'junkMailApp.filters',
    'junkMailApp.services',
    'junkMailApp.directives',
    'junkMailApp.controllers',
]).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/', {templateUrl: 'partials/welcome.html', controller: 'WelcomeCtrl'});
        $routeProvider.when('/messages/:messageId', {templateUrl: 'partials/message.html', controller: 'MessageCtrl'});
        $routeProvider.otherwise({redirectTo: '/'});
    }]);