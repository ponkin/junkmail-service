'use strict';

/* Controllers */

angular.module('junkMailApp.controllers', [])
    .controller('HeadCtrl', ['$scope', 'InboxService',
        function($scope, inboxService){
            var setMessageTitle = function(messages) {
                var newMsgCount = messages.reduce(function(previousValue, currentValue, index, array){
                    if(array[index].seen){
                        return previousValue
                    }else {
                        return previousValue + 1;
                    }
                }, 0);
                $scope.page_title = newMsgCount + " new messages"
            };
            inboxService.subscribe(function(message){
                inboxService.getMessages().then(setMessageTitle);
            });
            inboxService.onDelete(function(index){
                inboxService.getMessages().then(setMessageTitle);
            });
            inboxService.onMessageSeen(function(index){
                inboxService.getMessages().then(setMessageTitle);
            });
        }])
    .controller('InboxCtrl', ['$scope', 'InboxService',
        function($scope, inboxService) {
            $scope.messages = [];
            inboxService.subscribe(function(message){
                console.log("Received message "+ message);
                $scope.messages.push(message);
            });
            inboxService.getInboxName().then(function(name){
                $scope.inboxName = name;
            });
            inboxService.onDelete(function(index){
                console.log("Delete message number"+index);
                $scope.messages.splice(index, 1);
            });
        }
    ])
    .controller('WelcomeCtrl', ['$scope', 'InboxService',
        function($scope, inboxService) {
        }
    ])
.controller('MessageCtrl', ['$scope', '$routeParams', '$window', 'InboxService',
	function($scope, $routeParams, $window, inboxService) {
		inboxService.getMessages().then(function(messages) {
			$scope.message = messages[$routeParams.messageId];
            if($scope.message) {
                $scope.message['seen'] = true;
                inboxService.messageSeen($routeParams.messageId);
            }
		}, function(reason) {
			console.log("Error getting messages:" + reason);
		});
        $scope.deleteMessage = function(){
            inboxService.delMessage($routeParams.messageId);
            $window.history.back();
        };
	}
]);
