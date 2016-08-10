/**
 * Created by Oskar on 24-Mar-16.

 not working: css styling from external .css file
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
var d3 = require('d3');
var room_table = (function () {
    function room_table() {
    }
    room_table.draw = function (data) {
        var columnKeys = Object.keys(data);
        columnKeys.splice(columnKeys.indexOf('nowhere'), 1);
        columnKeys.splice(columnKeys.indexOf('waiting'), 1);
        var emptyrooms = this.listRooms(data, columnKeys);
        var roomTable = this.tabulate(emptyrooms, columnKeys);
    };
    room_table.listRooms = function (rawData, columnKeys) {
        var emptyrooms = [];
        for (var col = 0; col < columnKeys.length; col++) {
            emptyrooms.push([]);
            var rows = rawData[columnKeys[col]].length;
            for (var row = 0; row < rows; row++) {
                if (rawData[columnKeys[col]][row]['occupied'] == false) {
                    emptyrooms[col].push(rawData[columnKeys[col]][row]['room']); //push data to specific column
                }
            }
            emptyrooms[col].reverse(); //flip the room order in order to make pop() work. pull() doesn't exist.
        }
        return emptyrooms;
    };
    room_table.tabulate = function (emptyrooms, columnKeys) {
        var table = d3.select(this.divName).select("table");
        var thead = table.select("thead");
        var tbody = table.select("tbody");
        thead.selectAll("*").remove();
        tbody.selectAll("*").remove();
        //table.style("height", "10%"); // set to very low value to make the rows stack tightly
        var table_width = 16; // the widht to pad the department names to
        var department_translator = {};
        department_translator["medicineYellow"] = room_table.rightPad("Medicin Gul", table_width);
        department_translator["medicineBlue"] = room_table.rightPad("Medicin Blå", table_width);
        department_translator["ort_cast"] = room_table.rightPad("Gips", table_width + 3);
        department_translator["surgery"] = room_table.rightPad("Kirurg", table_width + 1);
        department_translator["triage"] = room_table.rightPad("Triage", table_width + 1);
        department_translator["acute"] = room_table.rightPad("Akut", table_width + 2);
        department_translator["jour"] = room_table.rightPad("Jour", table_width);
        department_translator["orthoped"] = room_table.rightPad("Ortoped", table_width + 2);
        department_translator["infection"] = room_table.rightPad("Infektion", table_width + 2);
        //create header row
        var tr = thead.append("tr");
        for (var i = 0; i < columnKeys.length; i++) {
            var th = thead.select("tr")
                .append("th")
                .attr("style", this.thStyle)
                .attr("colspan", 3)
                .text(department_translator[columnKeys[i]]);
            if (i % 2 == 0) {
                th.style("background-color", this.color1);
                th.style("background-color", this.color1);
            }
            else {
                th.style("background-color", this.color2);
                th.style("background-color", this.color2);
            }
        }
        // create rows(and content)
        var thereIsMore = true;
        for (var i = 0; i < 150 && thereIsMore; i++) {
            var tr = tbody.append("tr").style("height", "8%"); //create empty row
            thereIsMore = false;
            for (var col = 0; col < columnKeys.length; col++) {
                var room1 = emptyrooms[col].pop();
                var room2 = emptyrooms[col].pop();
                var room3 = emptyrooms[col].pop();
                var tr1 = tr.append("td")
                    .attr("style", this.tdStyle1)
                    .text(room1)
                    .style("font-size", "18px");
                var tr2 = tr.append("td")
                    .attr("style", this.tdStyle2)
                    .text(room2)
                    .style("font-size", "18px");
                var tr3 = tr.append("td")
                    .attr("style", this.tdStyle2)
                    .text(room3)
                    .style("font-size", "18px");
                if (col % 2 === 0) {
                    tr1.style("background-color", this.color1);
                    tr2.style("background-color", this.color1);
                    tr3.style("background-color", this.color1);
                }
                else {
                    tr1.style("background-color", this.color2);
                    tr2.style("background-color", this.color2);
                    tr3.style("background-color", this.color2);
                }
                if (room1 != undefined || room2 != undefined) {
                    thereIsMore = true;
                }
            }
        }
    };
    // Pads a string with blankspaces up to length n
    room_table.rightPad = function (s, n) {
        var len = s.length;
        for (var i = 0; i < n - len; i++) {
            s += "\u00A0"; // this is an immortal super-blankspace. trailing mortal blanksapces are ignored by d3
        }
        return s;
    };
    room_table.divName = "map_tableContainer";
    room_table.tdStyle1 = "padding: 0% 0% 0% 0.5%;"; //left cell
    room_table.tdStyle2 = "padding: 0% 0% 0% 0%;"; //right cell
    room_table.thStyle = "padding: 0 0% 0% 0; text-align:center;";
    room_table.color1 = "#C9C9C9";
    room_table.color2 = "#D9D9D9";
    room_table = __decorate([
        core_1.Component({
            selector: 'abra',
            template: "\n        <map_tableContainer style=\"height:100%; width:100%; margin:0 auto; display: block;\" >\n            <h2 style=\"height:20%; width:100%; display:block;\">Lediga rum</h2>\n            <table style=\"width:100%; font-size:120%;\">\n              <thead></thead>\n              <tbody></tbody>\n            </table>\n        </map_tableContainer>\n        ",
            styleUrls: ['app/globalcss/style.css'],
            styles: ["\n        .odd {\n            background-color: green;\n        }\n        .even {\n            background-color: red;\n        }\n        "]
        }), 
        __metadata('design:paramtypes', [])
    ], room_table);
    return room_table;
}());
exports.room_table = room_table;
//# sourceMappingURL=map_room_table.js.map