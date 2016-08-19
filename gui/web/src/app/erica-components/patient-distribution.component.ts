/**
 * Created by Linnea on 2016-08-11.
 */

import {
    Component,
    OnInit,
    OnDestroy
} from '@angular/core';
import * as d3 from 'd3';
import {SocketIO} from './socket-io';

@Component({
    selector: 'patient-distribution',
    template: `
    <div class = "rubrik">Fördelning av väntande patienter</div>
    <div>
    <svg class="patientDistributionGraph" style="display: block; margin:0 auto;" ></svg>
    </div>
    `,
    providers: [SocketIO]
})


export class patientDistributionGraphComponent implements OnInit, OnDestroy {

    private static scaleSVG(svg,endpoints){
        svg.attr("viewBox", endpoints[0] +" " +endpoints[1] +" " +endpoints[2] +" " +endpoints[3])
            .attr("preserveAspectRatio","xMidYMid meet")
            .classed("svg-content-responsive", true)
            .classed("svg-container", true) //container class to make it responsive
            .attr("height","100%")
            .attr("width", "100%")
        ;
    }

    static svgClass = '.patientDistributionGraph';


    ngOnInit() {
        console.log('onInit started');

        var sampleJson = {
            "data": [
                {
                    "Slot": "Triage",
                    "Undefined" : 10,
                    "Medicin" : 0,
                    "Medicin gul" : 0,
                    "Medicin bla" : 0,
                    "Kirurg" : 0,
                    "Ortoped" : 0,
                    "Jour" : 0
                },
                {
                    "Slot": "Läkare",
                    "Undefined" : 0,
                    "Medicin" : 2,
                    "Medicin gul" : 5,
                    "Medicin bla" : 4,
                    "Kirurg" : 7,
                    "Ortoped" : 3,
                    "Jour" : 2
                },
                {
                    "Slot": "Röntgen",
                    "Undefined" : 0,
                    "Medicin" : 0,
                    "Medicin gul" : 2,
                    "Medicin bla" : 0,
                    "Kirurg" : 1,
                    "Ortoped" : 4,
                    "Jour" : 2
                },
                {
                    "Slot": "Hemgång/avdelning",
                    "Undefined" : 0,
                    "Medicin" : 0,
                    "Medicin gul" : 1,
                    "Medicin bla" : 3,
                    "Kirurg" : 1,
                    "Ortoped" : 2,
                    "Jour" : 0
                }
            ]
        };

        var colors = {
            "colors":
            {
                "Undefined" : "#333333",
                "Medicin" : "#807F7F",
                "Medicin gul" : "#D3A32A",
                "Medicin bla" : "#3D7299",
                "Kirurg" : "#9C2726",
                "Ortoped" : "#346933",
                "Jour" : "#5A486D"
            }

        };

        patientDistributionGraphComponent.makeGraph(sampleJson, colors);

        //console.log("Default finished");

        /*SocketIO.subscribe('treatmentquota', function(data) {
            patientDistributionGraphComponent.makeGraph(data);
            console.log("hämtar data \n", data);
         });*/

        console.log('onInit finished');

    }

    ngOnDestroy() {
        console.log('run onDestroy');
        // Disconnect from bus
    }


/*           Funktions to make graph                */

    static makeGraph(data, colors) {
        //console.log("makeGraph initiated");

        // Set data sources
        var inData = data.data;
        var color2 = colors.colors;


        // Define sizes and margins
        var margin = {top: 20, right: 30, bottom: 50, left: 50},
            width = 500 - margin.left - margin.right,
            height = 400 - margin.top - margin.bottom;

        var canvas = d3.select(this.svgClass);
        this.scaleSVG(canvas,[0,0,width + margin.left + margin.right,height + margin.top + margin.bottom]);

        // Scale content to canvas
        var xScale = d3.scale.ordinal()
                .rangeRoundBands([0, width], .2),
            yScale = d3.scale.linear()
                .range([height, 0]);

        //console.log(1);

        var xAxis = d3.svg.axis()
                .scale(xScale)
                .orient("bottom")
                .ticks(5),
            yAxis = d3.svg.axis()
                .scale(yScale)
                .orient("left")
                .ticks(5);

        //console.log(2);

        // Add background to canvas
        canvas.append("rect")
            .attr("width", "100%")
            .attr("height", "100%")
            .attr("fill","lightgray")
            .attr("opacity", 0.15)
        ;

        //console.log(3);

        // Create graph area
        var graphBoard = canvas
            .append("g")
            .attr("fill", "black")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
            .attr("width", width)
            .attr("height", height);

        //console.log(4);


        var teams = d3.keys(inData[0]).filter(function(key) { return key !== "Slot"; });
        inData.forEach(function(d) {
            var y0 = 0;
            d.team = teams.map(function(name) { return {name: name, y0: y0, y1: y0 += +d[name]}; });
            d.total = d.team[d.team.length - 1].y1;
        });

        //console.log(5);

        // Set domains for scaling
        xScale.domain(inData.map(function(d) { return d["Slot"] }));
        yScale.domain([0, d3.max(inData, function(d) { return d.total})]).nice(5);

        //console.log(6);

        // Add axes to canvas
        graphBoard.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

        graphBoard.append("text")
            .attr("class", "x label")
            .attr("x", width/2)
            .attr("y",(height + margin.bottom))
            .attr("dy", "-1em")
            .style("text-anchor", "middle")
            .text("Vad patient väntar på")
        ;

        graphBoard.append("g")
            .attr("class", "y axis")
            .call(yAxis);

        graphBoard.append("text")
            .attr("class", "y label")
            .attr("x", -height/2)
            .attr("y",-margin.left)
            .attr("dy", "1.5em")
            .style("text-anchor", "middle")
            .attr("transform", "rotate(-90)")
            .text("Antal väntande patienter")
            .attr("font-size", "10px");

        //console.log(7);

        // Add bars with data
        var bars = graphBoard.selectAll(".Slot")
            .data(inData)
            .enter().append("g")
            .attr("class", "bar")
            .attr("transform", function(d) { return "translate(" + xScale(d["Slot"]) + ",0)"; });

        //console.log(8);

        bars.selectAll("rect")
            .data(function(d) {return d.team; })
            .enter().append("rect")
            .attr("width", xScale.rangeBand())
            .attr("y", function(d) { return yScale(d.y1); })
            .attr("height", function(d) { return yScale(d.y0) - yScale(d.y1); })
            .style("fill", function(d) {return color2[d.name]; });

        //console.log(9);

        // Add data labels on bars
        bars.append("text")
            .attr("y", function(d) { return yScale(d.total)})
            .attr("dy", "-.20em")
            .attr("dx", xScale.rangeBand()/2)
            .text(function(d) { return d.total })
            .attr("text-anchor", "middle")
            .attr("font-size", "16px");

        // add legend
        var legend = graphBoard
            .selectAll(".legend")
            .data(teams.reverse())
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) { return "translate(0," + i * 16 + ")"; })
            //.attr("height", 100)
            //.attr("width", 100)
            .style("font-size","12px");

        //console.log(10);

        legend//.selectAll('rect')
            .append("rect")
            .attr("x", width - 10)
            .attr("width", 12)
            .attr("height", 12)
            //.style("fill", color)
            .style("fill", function(d) {return color2[d]; });

        //console.log(11);

        legend//.selectAll('text')
            .append("text")
            .attr("x", width - 15)
            .attr("y", 9)
            .attr("dy", ".35em")
            .style("text-anchor", "end")
            .text(function(d) {return d; });

        //console.log("makeGraph finished");
    };

}



