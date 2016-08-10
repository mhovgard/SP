/**
 * Created by asasoderlund on 2016-04-06.
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
var core_1 = require('@angular/core');
var barchart_abstract_1 = require('./barchart_abstract');
var d3 = require('d3');
var barchart_medicin = (function (_super) {
    __extends(barchart_medicin, _super);
    function barchart_medicin() {
        _super.apply(this, arguments);
    }
    barchart_medicin.scaleSVG = function (svg, width, height, endpoints) {
        svg.attr("viewBox", endpoints[0] + " " + endpoints[1] + " " + endpoints[2] + " " + endpoints[3]);
        svg.attr("preserveAspectRatio", "xMaxYMax");
        svg.attr("height", height + "%");
        svg.attr("width", width + "%");
    };
    barchart_medicin.draw = function (rawData) {
        var rawData = rawData.bars;
        console.log("barchart:draw!", rawData);
        for (var i = 0; i < rawData.length; i++) {
            if (rawData[i].division == "Medicin Blå") {
                this.drawWithRefinedData(rawData[i]);
                return;
            }
        }
        console.log("Barchar_medicine: ERROR, could not find data");
    };
    barchart_medicin.drawWithRefinedData = function (jsonData) {
        var svg = d3.select(".barchart_medicine");
        this.scaleSVG(svg, 90, 90, [0, 30, 400, 300]);
        var nPatients = jsonData.total_patients;
        var h3_number = d3.select('.barchart_totNumber');
        h3_number.text("Patientantal: " + nPatients);
        var max = nPatients;
        var width = 500, chartWidth = 200, height = 300, chartHeight = height * 0.7, barSpace = chartWidth / 3, barWidth = barSpace * 0.9, fontSize = 11, legendSpace = height / 20, legendSize = legendSpace / 2;
        var chart = d3.select(".barchart_medicine");
        chart.selectAll("*").remove(); //delete garbage
        var x = d3.scale.ordinal()
            .domain(this.staplar)
            .rangeRoundBands([0, chartWidth], .1);
        var y = d3.scale.linear()
            .range([chartHeight, 0])
            .domain([0, max]);
        //Axis
        var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");
        var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left")
            .ticks(10)
            .outerTickSize(1)
            .tickFormat(function (d) {
            if (d % 1 == 0) {
                return d;
            }
        });
        var bar = chart
            .append("g")
            .attr("id", "data")
            .attr("id", "data")
            .attr("transform", "translate(50,50)"); //rotate
        bar.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + chartHeight + ")")
            .call(xAxis)
            .selectAll("text")
            .attr("font-size", fontSize * 1.30)
            .attr("dx", ".2m")
            .attr("dy", ".5em")
            .attr("transform", "rotate(-15)")
            .style("text-anchor", "end");
        bar.append("g")
            .attr("class", "y axis")
            .style({ 'stroke': 'black', 'fill': 'black', 'stroke-width': '1px' })
            .call(yAxis);
        //Legend
        var legend = chart.append("g")
            .attr("class", "legend")
            .attr("x", chartWidth + barWidth * 4)
            .attr("y", 50)
            .attr("height", 100)
            .attr("width", 100);
        legend.selectAll('g').data(barchart_abstract_1.Barchart.getMedLegend())
            .enter()
            .append('g')
            .each(function (d, i) {
            var g = d3.select(this);
            var x = chartWidth + barSpace;
            var rect = g.append("rect")
                .attr("x", x)
                .attr("y", i * legendSpace + (height - chartHeight) / 2)
                .attr("width", legendSize)
                .attr("height", legendSize);
            if (i == 0) {
                barchart_abstract_1.Barchart.svgStroke(rect, d[1]);
            }
            else {
                rect.style("fill", d[1]);
            }
            g.append("text")
                .attr("x", x + 15)
                .attr("y", i * legendSpace * 1.01 + (height - chartHeight) / 2 * 1.12)
                .style("fill", "black")
                .text(d[0]);
        });
        var barBox = bar.append("g")
            .attr("class", "chartArea");
        // -----------TRIAGE STATUS ----------------------
        var triage = [];
        triage[4] = 'red'; //Barchart.jsonKeys.red
        triage[3] = 'orange'; //Barchart.jsonKeys.orange;
        triage[2] = 'yellow';
        triage[1] = 'green';
        triage[0] = 'blue';
        var xCoord = 1 * barSpace - barWidth;
        var lastBox = barchart_abstract_1.Block.drawPile(jsonData, triage, barBox, y, chartHeight, barWidth, xCoord);
        //---------- INKOMMANDE
        var incoming = jsonData['incoming'];
        var incWidth = barWidth + 2 * barSpace;
        var incHeight = chartHeight - y(incoming);
        var incColor = "";
        var block = new barchart_abstract_1.Block(barBox, lastBox.x, lastBox.y - incHeight, incWidth, incHeight, incColor, incoming);
        block.stroke(barchart_abstract_1.Barchart.color_hash['incoming'][1]);
        //----------------------PATIENTSTATUS--------------------------
        var patientStatus = [];
        patientStatus[0] = 'noDoc';
        patientStatus[1] = 'hasDoc';
        patientStatus[2] = 'doneDoc';
        var xCoord = 2 * barSpace - barWidth;
        barchart_abstract_1.Block.drawPile(jsonData, patientStatus, barBox, y, chartHeight, barWidth, xCoord);
        // -----------RUMSFÖRDELNING ----------------------
        var roomArray = [];
        roomArray[0] = 'roomsHere';
        roomArray[1] = 'innerWaiting';
        roomArray[2] = 'atExam';
        roomArray[3] = 'roomsElse';
        var xCoord = 3 * barSpace - barWidth;
        barchart_abstract_1.Block.drawPile(jsonData, roomArray, barBox, y, chartHeight, barWidth, xCoord);
    }; //draw()
    barchart_medicin.staplar = (["Priofärg", "Läkarstatus", "Plats"]);
    barchart_medicin = __decorate([
        core_1.Component({
            selector: '.medbarchart',
            template: "\n        <h2 class=\"barchart_totNumber\" style=\"font-weight: bold; margin: 0 auto; height:10%; width:50%\">Patientantal: </h2>\n\t\t    <svg class='barchart_medicine' style=\"display:block; margin:0 auto;\"></svg>\n\t\t"
        }), 
        __metadata('design:paramtypes', [])
    ], barchart_medicin);
    return barchart_medicin;
}(barchart_abstract_1.Barchart));
exports.barchart_medicin = barchart_medicin;
//# sourceMappingURL=barchart_medicin.js.map