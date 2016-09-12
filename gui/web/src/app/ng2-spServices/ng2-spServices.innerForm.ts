/**
 * Created by oskar on 12/09/16.
 */
import { Component, Directive, ElementRef, Renderer, OnInit, Input, Inject } from '@angular/core';

@Component({
    selector: 'ng2-spServicesInnerForm',
    templateUrl: 'app/ng2-spServices/ng2-spServices.innerForm.html',
})


export class Ng2SpServicesInnerForm implements OnInit{
    private vm;

    private itemService;
    private spServicesService;
    private modelService;

    //@Input('vmVar') vmVar;
    @Input() vmVar;

    constructor(el: ElementRef, renderer: Renderer,
                @Inject('spServicesService') spServicesService,
                @Inject('modelService') modelService
    ) {
        this.spServicesService = spServicesService;
        this.modelService = modelService;
    }

    ngOnInit(){
        this.vm = this.vmVar;
        console.log(this.vm);
        this.spServicesFormController(this.vm,this.modelService,this.spServicesService);
    }

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

    private spServicesFormController(vm,modelService,spServicesService) {
        vm=vm.attributes.core;
        vm.reloadModelID = spServicesService.reloadModelID;
        vm.reloadSelectedItems = spServicesService.reloadSelectedItems;
        vm.domainToSelectFrom = [];
        console.log(vm);

        //this.activate(vm[0]);
    }


    private activate(vm){
        var x = vm.structure;
        if (this.isUndefined(x)){
            vm.isA = ""
        } else if (!this.isUndefined(x.ofType)){
            if (x.ofType == "Option[ID]" && vm.key == "model") {
                vm.isA = "Option[ID]Model";
                vm.attributes = this.isUndefined(x.default) ? vm.reloadModelID() : x.default;
                //core>includeIDAbles
            } else if (x.ofType == "List[ID]" && vm.key == "includeIDAbles") {
                vm.isA = "List[ID]includeIDAbles";
                vm.attributes = this.isUndefined(x.default) ? vm.reloadSelectedItems() : x.default;
                //Boolean
            } else if (x.ofType == "Boolean") {
                vm.isA = "Boolean";
                vm.attributes = this.isUndefined(x.default) ? false : x.default;
                //String
            } else if (x.ofType == "String") {
                vm.isA = "String";
                vm.attributes = this.isUndefined(x.default) ? "" : x.default;
                if(!this.isUndefined(x.domain) && x.domain.length > 0) {
                    vm.isA += "WithDomain";
                    vm.domainToSelectFrom = x.domain;
                }
                //Int
            } else if (x.ofType == "Int") {
                vm.isA = "Int";
                vm.attributes = this.isUndefined(x.default) ? 0 : x.default;
                if(!this.isUndefined(x.domain) && x.domain.length > 0) {
                    vm.isA += "WithDomain";
                    vm.domainToSelectFrom = x.domain;
                }
                //ID
            } else if (x.ofType == "ID") {
                vm.isA = "ID";
                vm.attributes = this.isUndefined(x.default) ? vm.reloadSelectedItems()[0] : x.default;
                //List[ID] and List[String]
            } else if (x.ofType == "List[ID]" || x.ofType == "List[String]") {
                vm.isA = "List[T]";
                vm.attributes = this.isUndefined(x.default) ? [] : x.default;
                //The rest
            } else {
                vm.isA = "KeyDef"; // för att testa
                vm.attributes = this.isUndefined(x.default) ? "" : x.default;
            }

        } else if (this.isObject(x)){
            vm.isA = "object";
            vm.attributes = {};
        } else {
            vm.isA = "something";
            vm.attributes = x;
        }
    }


    private isObject(obj:any){
        return Object.keys(obj).length > 0;
    }

    private isUndefined(obj:any){
        return obj === null;
    }


}

