"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var core_1 = require('@angular/core');
var ng2_spServices_innerForm_1 = require('./ng2-spServices.innerForm');
var Ng2SpServicesComponent = (function () {
    function Ng2SpServicesComponent(logger, spServicesService, modelService) {
        this.modelService = modelService;
        this.vm = {};
        this.vm.registeredServices = spServicesService.spServices; //From REST-api
        this.vm.showDetails = {};
        this.vm.openResponse = {};
        this.vm.currentProgess = {};
        this.vm.servicesAreRunnable = this.servicesAreRunnableFunc();
        this.vm.serviceAttributes = {};
        /*
         var vm = this;
         var dashboard = $scope.$parent.$parent.$parent.vm.dashboard;
         vm.widget = $scope.$parent.$parent.$parent.vm.widget; //For GUI

         vm.startSpService = startSPService; //To start a service. Name of service as parameter
         vm.currentProgess = {};

         vm.isServiceActive = isServiceActive;
         */
        for (var _i = 0, _a = this.vm.registeredServices; _i < _a.length; _i++) {
            var s = _a[_i];
            this.vm.showDetails[s.name] = false;
            this.vm.openResponse[s.name] = false;
        }
    }
    Ng2SpServicesComponent.prototype.startSPService = function (spService) {
        //Fill attributes with default values if spService directive has not been loaded.
        if (this.isUndefined(this.vm.serviceAttributes[spService.name])) {
            this.vm.serviceAttributes[spService.name] = this.fillAttributes(spService.attributes, "");
        }
        this.spServicesService.callService(spService, { "data": this.vm.serviceAttributes[spService.name] }, this.resp, this.prog);
        /* might not ever have worked:
         if (!this.isUndefined(this.vm.currentProgess[event.service])){
         delete this.vm.currentProgess[event.service];
         }*/
    };
    Ng2SpServicesComponent.prototype.resp = function (event) {
        console.log("RESP GOT: ");
        console.log(event);
        //            if (event.isa === 'Response') {
        //                for(var i = 0; i < event.ids.length; i++) {
        //                    if (!_.isUndefined(event.ids[i].sop)) {
        //                        var widgetKind = _.find(dashboardService.widgetKinds, {title: 'SOP Maker'});
        //                        var widgetStorage = {
        //                            sopSpec: event.ids[i]
        //                        };
        //                        dashboardService.addWidget(dashboard, widgetKind, widgetStorage);
        //                    }
        //                }
        //            }
        this.updateInfo(event);
    };
    Ng2SpServicesComponent.prototype.prog = function (event) {
        console.log("PROG GOT: ");
        console.log(event);
        this.updateInfo(event);
    };
    Ng2SpServicesComponent.prototype.updateInfo = function (event) {
        var error = "";
        if (!this.isUndefined(event.serviceError)) {
            error = event.serviceError.error;
        }
        var info = {
            service: event.service,
            reqID: event.reqID,
            info: event.attributes,
            error: error,
            type: event.isa,
            ids: event.ids
        };
        this.vm.currentProgess[event.service] = info;
    };
    Ng2SpServicesComponent.prototype.fillAttributes = function (structure, key) {
        var x = structure;
        if (this.isUndefined(x)) {
        }
        else if (!this.isUndefined(x.ofType)) {
            //core>model
            if (x.ofType == "Option[ID]" && key == "model") {
                //                    console.log("inside" + x.default);
                return this.isUndefined(x.default) ? this.spServicesService.reloadModelID() : x.default;
            }
            else if (x.ofType == "List[ID]" && key == "includeIDAbles") {
                //                    return this.isUndefined(x.default) ? spServicesService.reloadSelectedItems() : x.default;
                return this.spServicesService.reloadSelectedItems();
            }
            else if (x.ofType == "Boolean") {
                return this.isUndefined(x.default) ? false : x.default;
            }
            else if (x.ofType == "String") {
                return this.isUndefined(x.default) ? "" : x.default;
            }
            else if (x.ofType == "Int") {
                return this.isUndefined(x.default) ? 0 : x.default;
            }
            else if (x.ofType == "List[ID]" || x.ofType == "List[String]") {
                return this.isUndefined(x.default) ? [] : x.default;
            }
            else {
                return this.isUndefined(x.default) ? "" : x.default;
            }
        }
        else if (this.isObject(x)) {
            var localAttribute = {};
            for (var localKey in x) {
                var attrName = localKey;
                var attrValue = x[localKey];
                localAttribute[attrName] = this.fillAttributes(attrValue, attrName);
            }
            return localAttribute;
        }
        else {
            return x;
        }
    };
    Ng2SpServicesComponent.prototype.isObject = function (obj) {
        return Object.keys(obj).length > 0;
    };
    Ng2SpServicesComponent.prototype.isUndefined = function (obj) {
        return obj === null;
    };
    Ng2SpServicesComponent.prototype.servicesAreRunnableFunc = function () {
        return !(this.modelService.activeModel === null);
    };
    Ng2SpServicesComponent = __decorate([
        core_1.Component({
            selector: 'ng2-spServices',
            templateUrl: 'app/ng2-spServices/ng2-spServices.component.html',
            directives: [ng2_spServices_innerForm_1.Ng2SpServicesInnerForm]
        }),
        __param(0, core_1.Inject('logger')),
        __param(1, core_1.Inject('spServicesService')),
        __param(2, core_1.Inject('modelService')), 
        __metadata('design:paramtypes', [Object, Object, Object])
    ], Ng2SpServicesComponent);
    return Ng2SpServicesComponent;
}());
exports.Ng2SpServicesComponent = Ng2SpServicesComponent;
//# sourceMappingURL=ng2-spServices.component.js.map