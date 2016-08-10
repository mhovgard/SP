/**
 * Created by edvard on 2016-03-01.
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
var barchart_coordinator_1 = require('./barchart_coordinator');
var socket_io_1 = require('./socket-io');
var barchart_coordinatorComponent = (function () {
    function barchart_coordinatorComponent() {
    }
    barchart_coordinatorComponent.prototype.ngOnInit = function () {
        var jsonData = [
            { "division": "incoming",
                "incoming": 0,
                "has_doctor": 0,
                "no_doctor": 0,
                "klar": 0,
                "blue": 0,
                "green": 0,
                "yellow": 0,
                "orange": 0,
                "red": 0,
                "untriaged": 9,
                "total_patients": 9
            },
            {
                "division": "Medicin Blå",
                "incoming": 0,
                "has_doctor": 8,
                "no_doctor": 11,
                "klar": 4,
                "blue": 0,
                "green": 2,
                "yellow": 11,
                "orange": 8,
                "red": 2,
                "untriaged": 0,
                "total_patients": 23
            },
            {
                "division": "Medicin Gul",
                "incoming": 2,
                "has_doctor": 6,
                "no_doctor": 10,
                "klar": 4,
                "blue": 1,
                "green": 1,
                "yellow": 9,
                "orange": 8,
                "red": 1,
                "untriaged": 0,
                "total_patients": 22
            },
            {
                "division": "Kirurg",
                "incoming": 1,
                "has_doctor": 5,
                "no_doctor": 8,
                "klar": 3,
                "blue": 0,
                "green": 1,
                "yellow": 8,
                "orange": 5,
                "red": 2,
                "untriaged": 0,
                "total_patients": 17 // total_patients
            },
            { "division": "Ortoped", "incoming": 1, "has_doctor": 3, "no_doctor": 6, "klar": 0, "blue": 0, "green": 3, "yellow": 5, "orange": 1, "red": 0, "untriaged": 0, "total_patients": 10 },
            { "division": "Jour", "incoming": 2, "has_doctor": 1, "no_doctor": 5, "klar": 1, "blue": 0, "green": 1, "yellow": 3, "orange": 3, "red": 0, "untriaged": 0, "total_patients": 9 }];
        barchart_coordinator_1.barchart_coordinator.draw(jsonData);
        socket_io_1.SocketIO.subscribe('bar_graphs', function (data) {
            this.jsonData = data['bars'];
            barchart_coordinator_1.barchart_coordinator.draw(this.jsonData);
        });
    };
    barchart_coordinatorComponent = __decorate([
        core_1.Component({
            selector: 'coordbarchart',
            template: "<barchart_coordinator style=\"width:100%; height:100%;\"></barchart_coordinator>\n\t\t",
            directives: [barchart_coordinator_1.barchart_coordinator],
            providers: [socket_io_1.SocketIO]
        }), 
        __metadata('design:paramtypes', [])
    ], barchart_coordinatorComponent);
    return barchart_coordinatorComponent;
}());
exports.barchart_coordinatorComponent = barchart_coordinatorComponent;
//# sourceMappingURL=barchart_coordinator.component.js.map