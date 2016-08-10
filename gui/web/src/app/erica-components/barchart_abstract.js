/*
*  Oskar
*/
"use strict";
var Barchart = (function () {
    function Barchart() {
    }
    Barchart.getMedLegend = function () {
        var legendKeys = [];
        legendKeys = this.getCoordLegend();
        legendKeys.push(["", "none"]);
        legendKeys.push(this.color_hash['roomsElse']);
        legendKeys.push(this.color_hash['atExam']);
        legendKeys.push(this.color_hash['innerWaiting']);
        legendKeys.push(this.color_hash['roomsHere']);
        return legendKeys;
    };
    Barchart.getCoordLegend = function () {
        var legendKeys = [];
        legendKeys.push(this.color_hash['incoming']);
        legendKeys.push(["", "none"]);
        legendKeys.push(this.color_hash['doneDoc']);
        legendKeys.push(this.color_hash['hasDoc']);
        legendKeys.push(this.color_hash['noDoc']);
        return legendKeys;
    };
    Barchart.svgStroke = function (svg, color) {
        if (svg) {
            svg.style("fill", "none")
                .style("stroke", color)
                .style("stroke-dasharray", ("2, 2"))
                .style("stroke-width", "1.75px");
        }
    };
    Barchart.jsonKeys = {
        red: 'red',
        orange: 'orange',
        yellow: 'yellow',
        green: 'green',
        blue: 'blue',
        incoming: 'incoming',
        doneDoc: 'klar',
        hasDoc: 'has_doctor',
        noDoc: 'no_doctor',
        noTriage: 'untriaged',
        innerWaiting: 'inner_waiting_room',
        atExam: 'at_examination',
        roomsHere: 'rooms_here',
        roomsElse: 'rooms_elsewhere'
    };
    Barchart.color_hash = {
        doneDoc: ["Klara", "#fbfbfb"],
        hasDoc: ["Påtittade", "#C0C0C0"],
        noDoc: ["Opåtittade", "#808080"],
        blue: ["Blå", "#0040FF"],
        green: ["Grön", "#5FCC00"],
        yellow: ["Gul", "#FFFF00"],
        orange: ["Orange", "#FF8C00"],
        red: ["Röd", "#FF0000"],
        roomsHere: ["I rum här", "#285078"],
        innerWaiting: ["Inre väntrum", "#328CA5"],
        atExam: ["På Undersökning", "#97D2C8"],
        roomsElse: ["Annan plats", "#C3E6BE"],
        noTriage: ["Ej triage", "#333333"],
        incoming: ["Ej triagefärg", "#333333"]
    };
    return Barchart;
}());
exports.Barchart = Barchart;
var Block = (function () {
    function Block(parent, x, y, width, height, color, nOfPatients) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.width = width;
        this.height = height;
        this.fontSize = 15;
        this.color = color;
        this.nOfPatients = nOfPatients;
        this.fontColor = "black";
        if (this.color == this.fontColor) {
            this.fontColor = "white";
        }
        //console.log(this);
        if (nOfPatients > 0) {
            this.svgBlock = this.paintBlock(parent, x, y, width, height, color);
            this.svgText = this.paintText(parent, x, y, this.fontColor, this.fontSize, nOfPatients);
        }
        return this;
    }
    Block.prototype.paintBlock = function (parent, x, y, width, height, color) {
        var block = parent.append("rect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill", color);
        return block;
    };
    Block.prototype.paintText = function (parent, x, y, fontColor, fontSize, text) {
        var textField = parent.append("text")
            .attr("x", x)
            .attr("y", y + fontSize)
            .attr("font-size", fontSize)
            .attr("fill", fontColor)
            .attr("dx", ".5em")
            .text(text);
        return textField;
    };
    Block.prototype.setFontColor = function (fontColor) {
        if (this.svgText) {
            this.svgText.attr("fill", fontColor);
        }
    };
    Block.prototype.stroke = function (color) {
        Barchart.svgStroke(this.svgBlock, color);
    };
    Block.drawPile = function (jsonData, keyArray, parent, yAxis, chartHeight, barWidth, xCoord) {
        var yCoord;
        var lastY = chartHeight;
        for (var i = 0; i < keyArray.length; i++) {
            var data = jsonData[Barchart.jsonKeys[keyArray[i]]];
            var boxHeight = chartHeight - yAxis(data);
            yCoord = lastY - boxHeight;
            var lastBlock = new Block(parent, xCoord, yCoord, barWidth, boxHeight, Barchart.color_hash[keyArray[i]][1], data);
            lastY = lastBlock.y;
        }
        return lastBlock;
    };
    return Block;
}());
exports.Block = Block;
//# sourceMappingURL=barchart_abstract.js.map