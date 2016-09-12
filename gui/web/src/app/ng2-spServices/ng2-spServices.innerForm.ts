/**
 * Created by oskar on 12/09/16.
 */
import { Component, Directive, ElementRef, Renderer, Input, OnInit } from '@angular/core';

@Component({
    selector: 'ng2-spServicesInnerForm',
    templateUrl: 'app/ng2-spServices/ng2-spServices.innerForm.html',
})

@Directive({ selector: '[ng2-spServicesInnerForm]' })
export class Ng2SpServicesInnerForm {
    @Input('vmVar') vmVar:any;
    public vm;

    constructor(el: ElementRef, renderer: Renderer) {
        console.log("hej");
        console.log(this.vmVar);
    }


    private spServicesFormController(modelService,itemService,spServicesService) {
        var vm = this.vm;
        vm.reloadModelID = spServicesService.reloadModelID;
        vm.reloadSelectedItems = spServicesService.reloadSelectedItems;
        vm.domainToSelectFrom = [];

        activate();

        function activate(){
            whatIsIt();
        }

        function whatIsIt(){
            var x = vm.structure;
            if (this.isUndefined(x)){
                vm.isA = ""
            } else if (!this.isUndefined(x.ofType)){
                //core>model
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
    }


    private isObject(obj:any){
        return Object.keys(obj).length > 0;
    }

    private isUndefined(obj:any){
        return obj === null;
    }


}

