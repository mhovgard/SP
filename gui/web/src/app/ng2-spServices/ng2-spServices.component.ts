import { Component, Inject, OnInit } from '@angular/core';
import { Ng2SpServicesInnerForm } from './ng2-spServices.innerForm';

@Component({
    selector: 'ng2-spServices',
    templateUrl: 'app/ng2-spServices/ng2-spServices.component.html',
    directives: [Ng2SpServicesInnerForm]
})

export class Ng2SpServicesComponent {
    modelService;
    spServicesService: any;
    vm:any;

    constructor(
        @Inject('logger') logger,
        @Inject('spServicesService') spServicesService,
        @Inject('modelService') modelService
    ){
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

        for(let s of this.vm.registeredServices){
            this.vm.showDetails[s.name] = false;
            this.vm.openResponse[s.name] = false;
        }
    }

    private startSPService(spService) {
        //Fill attributes with default values if spService directive has not been loaded.
        if(this.isUndefined(this.vm.serviceAttributes[spService.name])) {
            this.vm.serviceAttributes[spService.name] = this.fillAttributes(spService.attributes,"");
            // console.log("vm.serviceAttributes[spService.name] " + JSON.stringify(vm.serviceAttributes[spService.name]));
        }

        this.spServicesService.callService(spService, {"data":this.vm.serviceAttributes[spService.name]}, this.resp, this.prog);
        /* might not ever have worked:
         if (!this.isUndefined(this.vm.currentProgess[event.service])){
         delete this.vm.currentProgess[event.service];
         }*/
    }

    private resp(event){
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
    }

    private prog(event){
        console.log("PROG GOT: ");
        console.log(event);

        this.updateInfo(event);
    }

    private updateInfo(event){
        var error = "";
        if (!this.isUndefined(event.serviceError)){
            error = event.serviceError.error
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
    }





    private fillAttributes(structure,key) {
        var x = structure;
        if (this.isUndefined(x)){
            //Do nothing
        } else if (!this.isUndefined(x.ofType)){
            //core>model
            if (x.ofType == "Option[ID]" && key == "model") {
//                    console.log("inside" + x.default);
                return this.isUndefined(x.default) ? this.spServicesService.reloadModelID() : x.default;
                //core>includeIDAbles
            } else if (x.ofType == "List[ID]" && key == "includeIDAbles") {
//                    return this.isUndefined(x.default) ? spServicesService.reloadSelectedItems() : x.default;
                return this.spServicesService.reloadSelectedItems();
                //Boolean
            } else if (x.ofType == "Boolean") {
                return this.isUndefined(x.default) ? false : x.default;
                //String
            } else if (x.ofType == "String") {
                return this.isUndefined(x.default) ? "" : x.default;
                //Int
            } else if (x.ofType == "Int") {
                return this.isUndefined(x.default) ? 0 : x.default;
                //List[ID] and List[String]
            } else if (x.ofType == "List[ID]" || x.ofType == "List[String]") {
                return this.isUndefined(x.default) ? [] : x.default;
                //The rest
            } else {
                return this.isUndefined(x.default) ? "" : x.default;
            }
        } else if (this.isObject(x)){
            var localAttribute = {};
            for(var localKey in x){
                var attrName = localKey;
                var attrValue = x[localKey];
                localAttribute[attrName] = this.fillAttributes(attrValue,attrName);
            }
            return localAttribute;
        } else {
            return x;
        }
    }

    private isObject(obj:any){
        return Object.keys(obj).length > 0;
    }

    private isUndefined(obj:any){
        return obj === null;
    }


    private servicesAreRunnableFunc() {
        return !(this.modelService.activeModel === null);
    }


}
