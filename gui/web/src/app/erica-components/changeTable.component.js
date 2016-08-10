/**
 * Created by Linnea on 2016-04-05.
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
var d3 = require('d3');
var changeTable = (function () {
    function changeTable() {
        this.patients = {
            blue: [
                {
                    patient_name: "Pat bbb",
                    patient_id: 3971342,
                    modification_field: "Läkare HELLU17",
                    minutes_since: "13",
                    current_location: "22"
                },
                {
                    patient_name: "Pat bbb",
                    patient_id: 3971342,
                    modification_field: "Läkare HELLU17",
                    minutes_since: "13",
                    current_location: "22"
                },
                {
                    patient_name: "Pat bbb",
                    patient_id: 3971342,
                    modification_field: "Läkare HELLU17",
                    minutes_since: "13",
                    current_location: "22"
                },
            ],
        };
    }
    // Detta sker när componenten initialiseras, dvs när länken till den klickas
    changeTable.prototype.ngOnInit = function () {
        changeTable.draw(this.patients);
        socket_io_1.SocketIO.subscribe('recent_changes', function (data) {
            changeTable.draw(data);
        });
    };
    changeTable.draw = function (data) {
        data = data.blue;
        var headerStyle = "font-size:180%;";
        var cellStyle = "padding: 0.5% 1% 0.5% 1% ; font-size: 160%;";
        var oddRowStyle = "background-color: #C9C9C9";
        var evenRowStyle = "background-color: #D9D9D9";
        var rowStyle = [oddRowStyle, evenRowStyle];
        var tableDiv = d3.select(this.parentDiv);
        var table = tableDiv.select('table');
        var thead = table.select('thead');
        var tbody = table.select('tbody');
        var columns = ["modification_field", "minutes_since", "current_location"];
        var colNames = ["Ändring", "Tid sedan", "Plats"];
        thead.selectAll("*").remove();
        tbody.selectAll("*").remove();
        //generate new stuff
        var headers = this.generateHeaders(thead, colNames, headerStyle);
        var rows = this.generateEmptyRows(tbody, data, rowStyle);
        var cells = this.generateCells(rows, columns, cellStyle);
    };
    changeTable.generateHeaders = function (thead, headerNames, headerStyle) {
        var headers = thead.append('tr');
        for (var i = 0; i < headerNames.length; i++) {
            headers.append('th').text(headerNames[i])
                .attr("style", headerStyle);
        }
    };
    changeTable.generateEmptyRows = function (tbody, data, rowStyle) {
        var odd = true;
        var rows = tbody.selectAll('tr')
            .data(data)
            .enter()
            .append('tr')
            .attr("style", function () {
            if (odd) {
                odd = false;
                return rowStyle[0];
            }
            else {
                odd = true;
                return rowStyle[1];
            }
        });
        return rows;
    };
    changeTable.generateCells = function (rows, columns, cellStyle) {
        var cells = rows.selectAll('td')
            .data(function (row) {
            return columns.map(function (column) {
                return { column: column, value: row[column] };
            });
        })
            .enter()
            .append("td")
            .attr("style", cellStyle)
            .html(function (d) {
            if (d.value == null) {
                return ".";
            }
            else if (d.column == "minutes_since") {
                return d.value + " min";
            }
            else if (d.column == "patient_name") {
                return " __ ";
            }
            else if (d.column == "patient_id") {
                return " xxx ";
            }
            return d.value;
        });
        return cells;
    };
    changeTable.parentDiv = '.change_table';
    changeTable = __decorate([
        core_1.Component({
            selector: 'latestTable',
            template: "\n        <div class=\"change_table\" style=\"width:100%;\">\n        <p style=\"font-size:200%; font-weight:bold; width:100%; padding-left:5%; \">Senaste \u00E4ndringar</p>\n        <table style=\"width:100%;\">\n            <thead></thead>\n            <tbody></tbody>\n        </table>\n        </div>"
        }), 
        __metadata('design:paramtypes', [])
    ], changeTable);
    return changeTable;
}());
exports.changeTable = changeTable;
//# sourceMappingURL=changeTable.component.js.map