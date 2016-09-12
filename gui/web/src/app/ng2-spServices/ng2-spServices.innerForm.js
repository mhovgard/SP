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
/**
 * Created by oskar on 12/09/16.
 */
var core_1 = require('@angular/core');
var Ng2SpServicesInnerForm = (function () {
    function Ng2SpServicesInnerForm(el, renderer, spServicesService, modelService) {
        this.spServicesService = spServicesService;
        this.modelService = modelService;
    }
    Ng2SpServicesInnerForm.prototype.ngOnInit = function () {
        this.vm = this.vmVar;
        console.log(this.vm);
        this.spServicesFormController(this.vm, this.modelService, this.spServicesService);
    };
    /* //not in use atm.
     private compile(tElement, tAttr, transclude) {
     var contents = tElement.contents().remove();
     var compiledContents;
     return function(scope, iElement, iAttr) {
     if(!compiledContents) {
     //compiledContents = $compile(contents, transclude);
     }
     compiledContents(scope, function(clone, scope) {
     iElement.append(clone);
     });
     };
     }
     */
    Ng2SpServicesInnerForm.prototype.spServicesFormController = function (vm, modelService, spServicesService) {
        vm = vm.attributes.core;
        vm.reloadModelID = spServicesService.reloadModelID;
        vm.reloadSelectedItems = spServicesService.reloadSelectedItems;
        vm.domainToSelectFrom = [];
        console.log(vm);
        //this.activate(vm[0]);
    };
    Ng2SpServicesInnerForm.prototype.activate = function (vm) {
        var x = vm.structure;
        if (this.isUndefined(x)) {
            vm.isA = "";
        }
        else if (!this.isUndefined(x.ofType)) {
            if (x.ofType == "Option[ID]" && vm.key == "model") {
                vm.isA = "Option[ID]Model";
                vm.attributes = this.isUndefined(x.default) ? vm.reloadModelID() : x.default;
            }
            else if (x.ofType == "List[ID]" && vm.key == "includeIDAbles") {
                vm.isA = "List[ID]includeIDAbles";
                vm.attributes = this.isUndefined(x.default) ? vm.reloadSelectedItems() : x.default;
            }
            else if (x.ofType == "Boolean") {
                vm.isA = "Boolean";
                vm.attributes = this.isUndefined(x.default) ? false : x.default;
            }
            else if (x.ofType == "String") {
                vm.isA = "String";
                vm.attributes = this.isUndefined(x.default) ? "" : x.default;
                if (!this.isUndefined(x.domain) && x.domain.length > 0) {
                    vm.isA += "WithDomain";
                    vm.domainToSelectFrom = x.domain;
                }
            }
            else if (x.ofType == "Int") {
                vm.isA = "Int";
                vm.attributes = this.isUndefined(x.default) ? 0 : x.default;
                if (!this.isUndefined(x.domain) && x.domain.length > 0) {
                    vm.isA += "WithDomain";
                    vm.domainToSelectFrom = x.domain;
                }
            }
            else if (x.ofType == "ID") {
                vm.isA = "ID";
                vm.attributes = this.isUndefined(x.default) ? vm.reloadSelectedItems()[0] : x.default;
            }
            else if (x.ofType == "List[ID]" || x.ofType == "List[String]") {
                vm.isA = "List[T]";
                vm.attributes = this.isUndefined(x.default) ? [] : x.default;
            }
            else {
                vm.isA = "KeyDef"; // för att testa
                vm.attributes = this.isUndefined(x.default) ? "" : x.default;
            }
        }
        else if (this.isObject(x)) {
            vm.isA = "object";
            vm.attributes = {};
        }
        else {
            vm.isA = "something";
            vm.attributes = x;
        }
    };
    Ng2SpServicesInnerForm.prototype.isObject = function (obj) {
        return Object.keys(obj).length > 0;
    };
    Ng2SpServicesInnerForm.prototype.isUndefined = function (obj) {
        return obj === null;
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Object)
    ], Ng2SpServicesInnerForm.prototype, "vmVar", void 0);
    Ng2SpServicesInnerForm = __decorate([
        core_1.Component({
            selector: 'ng2-spServicesInnerForm',
            templateUrl: 'app/ng2-spServices/ng2-spServices.innerForm.html',
        }),
        __param(2, core_1.Inject('spServicesService')),
        __param(3, core_1.Inject('modelService')), 
        __metadata('design:paramtypes', [core_1.ElementRef, core_1.Renderer, Object, Object])
    ], Ng2SpServicesInnerForm);
    return Ng2SpServicesInnerForm;
}());
exports.Ng2SpServicesInnerForm = Ng2SpServicesInnerForm;
//# sourceMappingURL=ng2-spServices.innerForm.js.map