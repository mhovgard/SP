'use strict';

/**
 * @ngdoc function
 * @name spGuiApp.controller:SiteCtrl
 * @description
 * # SitecontrollerCtrl
 * Controller of the spGuiApp
 */
angular.module('spGuiApp')
  .controller('SiteCtrl', function ($scope, $routeParams, $location, $rootScope, spTalker, $modal, USER_ROLES, AuthService, Session) {
    $scope.currentUser = null;
    $scope.userRoles = USER_ROLES;
    $scope.isAuthorized = AuthService.isAuthorized;
    $scope.noOfOpenedTabs = 0;
    $scope.tabs = [];
    $scope.currentUser = [Session.userId, Session.userRole];
    $scope.spTalker = spTalker;

    $scope.addTab = function() {
      var windowArray = [];
      $scope.noOfOpenedTabs++;
      $scope.tabs.push({title: 'Model ' + $scope.noOfOpenedTabs, windowArray: windowArray, active: true})
    };
    $scope.addTab();

    $scope.closeTab = function(tab) {
      var index = $scope.tabs.indexOf(tab);
      if(tab.windowArray.length > 0) {
        if(confirm('You are about to close a tab with open windows inside. Sure?')) {
          $scope.tabs.splice(index, 1);
        }
      } else {
        $scope.tabs.splice(index, 1);
      }
    };

    $rootScope.vars = {
      isLoginPage : false
    };
    $scope.isLoginPage = $rootScope.vars.isLoginPage;

    /*$scope.$watch(function() {
        return $rootScope.vars.isLoginPage;
      }, function(data) {
          $scope.isLoginPage = data;
      }, true);*/

    /*$scope.$watch(function() {
        return Session.id;
    }, function(data) {
        $scope.currentUser = [Session.userId, Session.userRole];
    }, true);*/

    $scope.headerUrl = 'views/header.html';

    /*$scope.$watch(
      function() { return spTalker.activeModel; },
      function(data) { $scope.activeModel = data; },
      true
    );*/

    $scope.openModelList = function () {
      var modalInstance = $modal.open({
        templateUrl: 'views/modellist.html',
        controller: ModellistCtrl
      });
    };
    $scope.openModelList();

    $scope.openSettings = function () {
      var modalInstance = $modal.open({
        templateUrl: 'views/settings.html',
        controller: 'SettingsCtrl'
      });
    };

    document.addEventListener('invalid', (function(){
      return function(e){
        //prevent the browser from showing default error bubbles/hints on input validation
        e.preventDefault();
      };
    })(), true);

    $scope.broadcastEvent = function(eventName) {
      $rootScope.$broadcast(eventName);
    };

  });