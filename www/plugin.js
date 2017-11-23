"use strict";

module.exports = {
    authorize: function(options, success, failure) {
        cordova.exec(success, failure, 'AVSLogin', 'authorize', [options]);
    },
    getToken: function(success, failure) {
        cordova.exec(success, failure, 'AVSLogin', 'getToken');
    },
    signOut: function(success, failure) {
        cordova.exec(success, failure, 'AVSLogin', 'signOut');
    }
};