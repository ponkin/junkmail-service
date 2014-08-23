'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('junkMailApp.services',[])
	.value('version', '0.3')
	.value('base_url', 'ws://localhost:6696/messages')
	.factory('InboxService', [ '$q', 'base_url',
		function($q, base_url) {
            var _subscribers = [];
            var _ondelete = [];
            var _onseen = [];
            var _messages = $q.defer();
            var _inboxName = $q.defer();
            var ws = new WebSocket(base_url);

            ws.onopen = function(){
                console.log("Socket has been opened!");
                _messages.resolve([]);
            };

            ws.onmessage = function(message) {
                console.log(message.data);
                var email = angular.fromJson(message.data);
                if(email.inboxName != null){
                    _inboxName.resolve(email.inboxName);
                }else{
                    _messages.promise.then(function(messages) {
                        messages.push(email)
                    });
                    angular.forEach(_subscribers, function (cb) {
                        cb(email);
                    });
                }
            };
			return {
                delMessage: function(index){
                    _messages.promise.then(function(messages){
                        messages.splice(index, 1);
                        angular.forEach(_ondelete, function (cb) {
                            cb(index);
                        });
                    });
                },
                messageSeen : function(index){
                    _messages.promise.then(function(messages){
                        messages[index]['seen'] = true;
                        angular.forEach(_onseen, function (cb) {
                            cb(index);
                        });
                    });
                },
                onMessageSeen: function(cb){
                    _onseen.push(cb);
                },
                onDelete: function(cb){
                    _ondelete.push(cb);
                },
                getInboxName: function(){
                    return _inboxName.promise;
                },
				getMessages: function() {
					return _messages.promise;
				},
                subscribe: function (cb) {
                    _subscribers.push(cb);
                }
			};
		}
	]);
