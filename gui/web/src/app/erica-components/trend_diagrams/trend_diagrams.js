/**
 * Created by edvard on 2016-04-05.
 */
"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
/*
 TTT - < 20 min = BRA, 20-40 min = OK, >40 min = Inte bra

 TTL – (1) < 1h = BRA, 1h-2h = OK, >2h = Inte bra
        (2)  < 1h = BRA, >= 1 Inte bra

 TTK – (1) < 3h 30 min = BRA,  3h 30 min – 4h = OK, >4h = Inte bra
        (2) < 3h 30 min = BRA, >=3h 30 min Inte bra

 TTA – (1) < 30 min BRA, 30-60 min = OK, >60 min = Inte bra
        (2)  <30 min = BRA, >=30 min Inte bra

 TVT – (1) < 4h = BRA, 4-6h = OK, >6h = Inte bra
        (2) <4h = BRA, >=4h Inte bra
 */
var core_1 = require("@angular/core");
var trend_diagram_1 = require('./trend_diagram');
var TTKDiagram = (function (_super) {
    __extends(TTKDiagram, _super);
    function TTKDiagram() {
        _super.apply(this, arguments);
        this.selector = ".ttk-chart";
        this.ttk_ylims = [5, 3];
        this.data = {
            'trend': [
                { 'x': -2, 'y': 5 },
                { 'x': -1, 'y': 6 },
                { 'x': 0, 'y': 4 }
            ],
            'prediction': [
                { 'x': 0, 'y': 4 },
                { 'x': 1, 'y': 6 }
            ],
            'times': { 'median': 3.9, 'Gul': 4.5, 'Bla': 6, 'Ki': 4.2, 'Ort': 3.5, 'Jour': 3.2 }
        };
    }
    TTKDiagram.prototype.getMarkerColors = function () {
        return MarkerColors();
    };
    TTKDiagram.prototype.drawDummy = function () {
        _super.prototype.draw.call(this, this.data, this.selector, this.ttk_ylims);
    };
    TTKDiagram.prototype.draw = function (data) {
        var ttk_ylims = [300, 100]; //TTK – (1) < 3h 30 min = BRA,  3h 30 min – 4h = OK, >4h = Inte bra
        _super.prototype.draw.call(this, data, this.selector, ttk_ylims);
    };
    TTKDiagram = __decorate([
        core_1.Component({
            selector: 'ttkdiagram',
            template: '<div style="display:table;margin:auto;" class="ttk-chart"></div>'
        }), 
        __metadata('design:paramtypes', [])
    ], TTKDiagram);
    return TTKDiagram;
}(trend_diagram_1.TrendDiagram));
exports.TTKDiagram = TTKDiagram;
var TTDDiagram = (function (_super) {
    __extends(TTDDiagram, _super);
    function TTDDiagram() {
        _super.apply(this, arguments);
        this.ttd_ylims = [30, 20]; //test
        this.selector = ".ttd-chart";
        this.data = {
            'trend': [
                { 'x': -2, 'y': 5 },
                { 'x': -1, 'y': 6 },
                { 'x': 0, 'y': 4 }
            ],
            'prediction': [
                { 'x': 0, 'y': 4 },
                { 'x': 1, 'y': 6 }
            ],
            'times': { 'median': 3.9, 'Gul': 4.5, 'Bla': 6, 'Ki': 4.2, 'Ort': 3.5, 'Jour': 3.2 }
        };
    }
    TTDDiagram.prototype.getMarkerColors = function () {
        return MarkerColors();
    };
    TTDDiagram.prototype.drawDummy = function () {
        _super.prototype.draw.call(this, this.data, this.selector, this.ttd_ylims);
    };
    TTDDiagram.prototype.draw = function (data) {
        var ttd_ylims = [120, 60]; //TTL – (1) < 1h = BRA, 1h-2h = OK, >2h = Inte bra
        _super.prototype.draw.call(this, data, this.selector, ttd_ylims);
    };
    TTDDiagram = __decorate([
        core_1.Component({
            selector: 'ttddiagram',
            template: '<div style="display:table;margin:auto;" class="ttd-chart"></div>'
        }), 
        __metadata('design:paramtypes', [])
    ], TTDDiagram);
    return TTDDiagram;
}(trend_diagram_1.TrendDiagram));
exports.TTDDiagram = TTDDiagram;
var TTTDiagram = (function (_super) {
    __extends(TTTDiagram, _super);
    function TTTDiagram() {
        _super.apply(this, arguments);
        this.ttt_ylims = [40, 20]; //test
        this.selector = ".ttt-chart";
        this.data = {
            'trend': [
                { 'x': -2, 'y': 5 },
                { 'x': -1, 'y': 6 },
                { 'x': 0, 'y': 4 }
            ],
            'prediction': [
                { 'x': 0, 'y': 4 },
                { 'x': 1, 'y': 6 }
            ],
            'times': { 'median': 3.9 }
        };
    }
    TTTDiagram.prototype.getMarkerColors = function () {
        return 0;
    };
    TTTDiagram.prototype.drawDummy = function () {
        _super.prototype.draw.call(this, this.data, this.selector, this.ttt_ylims);
    };
    TTTDiagram.prototype.draw = function (data) {
        var ttt_ylims = [40, 20]; //   TTT - < 20 min = BRA, 20-40 min = OK, >40 min = Inte bra
        _super.prototype.draw.call(this, data, this.selector, this.ttt_ylims);
    };
    TTTDiagram = __decorate([
        core_1.Component({
            selector: 'tttdiagram',
            template: '<div style="display:table;margin:auto;" class="ttt-chart"></div>'
        }), 
        __metadata('design:paramtypes', [])
    ], TTTDiagram);
    return TTTDiagram;
}(trend_diagram_1.TrendDiagram));
exports.TTTDiagram = TTTDiagram;
function MarkerColors() {
    return {
        'median': 'gray',
        'Gul': 'yellow',
        'Blue': 'blue',
        'Ki': 'red',
        'Ort': 'green',
        'Jour': 'purple'
    };
}
//# sourceMappingURL=trend_diagrams.js.map