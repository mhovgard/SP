/**
 * Created by edvard on 2016-04-05.
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
var trend_diagrams_1 = require('./trend_diagrams');
var socket_io_1 = require('../socket-io');
var TrendDiagrams = (function () {
    function TrendDiagrams() {
    }
    TrendDiagrams.prototype.ngOnInit = function () {
        TrendDiagrams.reDraw(null);
        console.log("start to draw!");
        socket_io_1.SocketIO.subscribe('coordinator_line_graph', function (data) {
            console.log(data);
            TrendDiagrams.reDraw(data);
        });
    };
    TrendDiagrams.reDraw = function (data) {
        var tttD = new trend_diagrams_1.TTTDiagram();
        var ttdD = new trend_diagrams_1.TTDDiagram();
        var ttkD = new trend_diagrams_1.TTKDiagram();
        if (data == null) {
            ttkD.drawDummy();
            ttdD.drawDummy();
            tttD.drawDummy();
        }
        else {
            ttkD.draw(data['ttk']);
            ttdD.draw(data['ttl']);
            tttD.draw(data['ttt']);
        }
    };
    TrendDiagrams = __decorate([
        core_1.Component({
            selector: 'trend-diagrams',
            template: "\n        <h2 style=\"padding:0px;\">V\u00E4ntetider -- trend</h2>\n        <div style=\"margin: 0 auto; display:block; width:90%;\" >\n            <h3>TTK</h3>\n            <ttkdiagram></ttkdiagram>\n            <h3>TTL</h3>\n            <ttddiagram></ttddiagram>\n            <h3>TTT</h3>\n            <tttdiagram></tttdiagram>\n        </div>\n        ",
            styles: ["\n        h3 {\n            float:left;\n        }\n    "],
            //styleUrls: ['app/trend_diagrams/trend_diagrams.css'],
            directives: [trend_diagrams_1.TTKDiagram, trend_diagrams_1.TTDDiagram, trend_diagrams_1.TTTDiagram]
        }), 
        __metadata('design:paramtypes', [])
    ], TrendDiagrams);
    return TrendDiagrams;
}());
exports.TrendDiagrams = TrendDiagrams;
//# sourceMappingURL=trend_diagrams.component.js.map