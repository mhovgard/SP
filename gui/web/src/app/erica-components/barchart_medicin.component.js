/**
 * Created by asasoderlund on 2016-04-06.
 */
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
var core_1 = require('@angular/core');
var socket_io_1 = require('./socket-io');
//import * as d3 from 'd3';
var barchart_medicin_1 = require('./barchart_medicin');
var barchart_medicinComponent = (function () {
    function barchart_medicinComponent() {
    }
    /*
     blue    :     0
     divisio     :     "Medicin Blå"
     green     :     0
     has_doctor     :     2
     incoming     :     0
     klar     :     3
     no_doctor     :     2
     orange     :     4
     red     :     0
     total_patients     :     7
     untriaged     :     0
     yellow     :     3

     */
    barchart_medicinComponent.prototype.ngOnInit = function () {
        barchart_medicin_1.barchart_medicin.drawWithRefinedData(barchart_medicinComponent.jsonData);
        socket_io_1.SocketIO.subscribe('bar_graphs', function (data) {
            barchart_medicin_1.barchart_medicin.draw(data);
        });
    };
    barchart_medicinComponent.jsonData = {
        "divison": "Medicin Blå",
        "incoming": 2,
        "has_doctor": 8,
        "no_doctor": 11,
        "klar": 4,
        "blue": 1,
        "green": 2,
        "yellow": 11,
        "orange": 8,
        "red": 1,
        "untriaged": 0,
        "rooms_here": 7,
        "inner_waiting_room": 12,
        "at_examination": 2,
        "rooms_elsewhere": 2,
        "total_patients": 25
    };
    barchart_medicinComponent = __decorate([
        core_1.Component({
            selector: 'medbarchart',
            template: "\n        <div class=\"medbarchart\" style=\"margin:0 auto;\"></div>\n\t\t",
            directives: [barchart_medicin_1.barchart_medicin]
        }), 
        __metadata('design:paramtypes', [])
    ], barchart_medicinComponent);
    return barchart_medicinComponent;
}());
exports.barchart_medicinComponent = barchart_medicinComponent;
//# sourceMappingURL=barchart_medicin.component.js.map