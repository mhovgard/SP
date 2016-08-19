/**
 * Created by Linnea on 2016-08-11.
 */

/*
 INKOMMANDE DATA behöver se ut som JSON i exemplet.
 quota är (#event: inkommande/ (#event: inkommande + #event: dragna/behandlade) - 0.5) * 2
 vid varje mättillfälle för senaste 30 minuterna. Ev dynamiskt så att det är längre "slottar" på natten.
 Visa värden för senaste 2 timmar ( alltså 2.5 timmar data), som default, men vore bra om man kunde ändra till
 8h, 12h, 24h, 48h etc.
 */


import {
    Component,
    OnInit,
    OnDestroy
} from '@angular/core';
import * as d3 from 'd3';
import {SocketIO} from './socket-io';


@Component({
    selector: 'inflow-outflow-graph',
    template: `
    <div class ="rubrik" >Förhållande mellan inkommande och behandlade patienter</div>
    
    <div>
    <svg class="treatmentQuotaGraph" style="display: block; margin:0 auto;" ></svg>    
    </div>
    `,
    providers: [SocketIO]
})


export class inflowOutflowGraphComponent implements OnInit, OnDestroy {

    makeGraph: (data) => void;
    times: any;
    quota: number;

    private static scaleSVG(svg,width,height,endpoints){
        svg.attr("viewBox", endpoints[0] +" " +endpoints[1] +" " +endpoints[2] +" " +endpoints[3])
            .attr("preserveAspectRatio","xMaxYMax meet")
            .classed("svg-content-responsive", true)
            .classed("svg-container", true) //container class to make it responsive
            .attr("height",height +"%")
            .attr("width",width +"%")
        ;
    }

    constructor() {
        var margin = {top: 20, right: 30, bottom: 30, left: 50},
            width = 500 - margin.left - margin.right,
            height = 400 - margin.top - margin.bottom;

        var svgClass = '.treatmentQuotaGraph';

        // Function that creates graph
        this.makeGraph = (data) => {

            var canvas = d3.select(svgClass);
            inflowOutflowGraphComponent.scaleSVG(canvas,100,100,
                [0,0,width + margin.left + margin.right,height + margin.top + margin.bottom]); //TODO Scalable

            var inData = data.data;

            // Scale content to canvas
            var xScale = d3.scale.linear()
                    .range([0, width]),
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

            // function for the y grid lines
            var grids = d3.svg.axis()
                .scale(yScale)
                .orient("left")
                .ticks(9)
                .tickSize(-width, 0)
                .tickFormat("")
                ;


            // Create line graph and scale it to window
            var valueLine = d3.svg.line()
                .x(function(d) { return xScale(d.times); })
                .y(function(d) { return yScale(d.quota); })
                .interpolate("cardinal") // Soften curve
                ;

            //console.log(3);

            // Add background to canvas
            canvas.append("rect")
                .attr("width", "100%")
                .attr("height", "100%")
                .attr("fill","aliceblue")
                ;

            // Create canvas/background for graph
            var graphBoard = canvas
                .append("g")
                .attr("fill", "black")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
                .attr("width", width)
                .attr("height", height);
            //console.log(4);

            // Identify max and min values for axes and set domains
            var xmin = d3.min(inData, function(d) { return d3.min(d['values'], function(d) { return d['times']; });});
            var xmax = d3.max(inData, function(d) { return d3.max(d['values'], function(d) { return d['times']; });});

            var ymin = -1;
            var ymax = 1;

            xScale.domain([xmin,xmax]);
            yScale.domain([ymin,ymax]);

            //console.log(6);

            // Add axes to canvas
            graphBoard.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height/2 + ")")
                .call(xAxis);


            graphBoard.append("g")
                .attr("class", "y axis")
                .call(yAxis);

            graphBoard.append("text")
                .attr("class", "y label")
                .attr("x", -height/4)
                .attr("y",-margin.left)
                .attr("dy", "1.5em")
                .style("text-anchor", "middle")
                .attr("transform", "rotate(-90)")
                .text("Fler inkommande")
            ;

            graphBoard.append("text")
                .attr("class", "y label")
                .attr("x", -3* height/4)
                .attr("y",-margin.left)
                .attr("dy", "1em")
                .style("text-anchor", "middle")
                .attr("transform", "rotate(-90)")
                .text("Fler behandlade")
            ;


            // Draw the y Grid lines
            graphBoard.append("g")
                .attr("class", "grid")
                .call(grids)
                .attr('stroke', 'lightgray')
                .attr('opacity', 0.7)
                .attr('stroke-width', 0.3);


            //console.log(7);

            // Identify and add data and create paths
            inData.forEach(function (dataSource) {

                //console.log(8);

                graphBoard.append('g')
                    .append("path")
                    .attr("class", "line")
                    .attr("stroke-width", 1.5)
                    .attr("stroke",dataSource['color'])
                    .attr("fill", "none")
                    .attr("d", valueLine(dataSource['values']))
                ;
            });

            //console.log(9);

            // add legend
            var legend = graphBoard.append("g")
                .attr("class", "legend")
                .attr("height", 100)
                .attr("width", 100)
                .attr('transform', "translate(0," + (height + margin.bottom/2 - inData.length * 16) + ")")
                .style("font-size","12px");

            //console.log(10);

            legend.selectAll('rect')
                .data(inData)
                .enter()
                .append("rect")
                .attr("x", width - 10)
                .attr("y", function(d, i){ return i *  16;})
                .attr("width", 12)
                .attr("height", 12)
                .style("fill", function(d, i) {
                    var color = inData[i]["color"];
                    return color;
                });

            //console.log(11);

            legend.selectAll('text')
                .data(inData)
                .enter()
                .append("text")
                .attr("x", width - 15)
                .attr("y", function(d, i){ return i *  16 + 9;})
                .text(function(d, i) {
                    //var text = inData[i]["label"];
                    return inData[i]["label"];
                })
                .attr("text-anchor","end");
        };

    };

    ngOnInit() {
        console.log('onInit started');

        var sampleJson = {
                "data": [
                    {
                        "values": [ { "times": 1,   "quota": -.2},  { "times": 2,  "quota": 0},
                            { "times": 3,  "quota": -.10}, { "times": 4,  "quota": .10},
                            { "times": 5,  "quota": .1},  { "times": 6, "quota": -.30}],
                        "label": "Medicin gul",
                        "color": "#feff57"
                    },
                    {
                        "values": [ { "times": 1,   "quota": -.4},  { "times": 2,  "quota": -.6},
                            { "times": 3,  "quota": .10}, { "times": 4,  "quota": .40},
                            { "times": 5,  "quota": .0},  { "times": 6, "quota": -.30}],
                        "label": "Kirurg",
                        "color": "#db002c"
                    },
                    {
                        "values": [ { "times": 1,   "quota": .5},  { "times": 2,  "quota": .20},
                            { "times": 3,  "quota": .10}, { "times": 4,  "quota": .40},
                            { "times": 5,  "quota": .9},  { "times": 6, "quota": .60}],
                        "label": "Medicin blå",
                        "color": "#6557ff"
                    },
                    {
                        "values": [ { "times": 1,   "quota": .5},  { "times": 2,  "quota": .20},
                            { "times": 3,  "quota": .50}, { "times": 4,  "quota": -.40},
                            { "times": 5,  "quota": .3},  { "times": 6, "quota": .70}],
                        "label": "Mwejhsd",
                        "color": "#000"
                    }
                ]
            };

        this.makeGraph(sampleJson);




        //console.log("Default finished");

        /*SocketIO.subscribe('treatmentquota', function(data) {
            this.makeGraph(data);
            console.log("hämtar data \n", data);
        });*/

        console.log('onInit finished');

    }

    ngOnDestroy() {
        console.log('run onDestroy');
        //Disconnect from bus
    }

}



