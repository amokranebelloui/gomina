
import React from 'react';
import * as d3 from 'd3';

import {Toggle} from './gomina';

import './archi-diagram.css'

class Diagram extends React.Component {
    constructor(props){
        super(props);
        this.createDiagram = this.createDiagram.bind(this)
        this.state = {active: true}
    }
    componentDidMount() {
        this.createDiagram()
    }
    componentDidUpdate() {
        // update ?
        //console.info("diagram prop updated")
    }
    componentWillUnmount() {
        var el = this.node;
        console.info(el);
        //d3.destroy(el); // Doesn't work
        //d3.select("svg").remove(); // Is it necessary?
    }
    createDiagram() {
        //const svg1 = this.node;
        const positions = this.props.components;
        const dependencies = this.props.dependencies;
        var svg = d3.select(this.node);

        //console.info("$$", svg, d3.select(svg1));
        var comps = svg.selectAll("g node-group").data(positions)

        svg.selectAll("line").data(dependencies).enter()
            .append("line")
            .attr("x1", function (d) { return positions[d.from].x; })
            .attr("x2", function (d) { return positions[d.to].x; })
            .attr("y1", function (d) { return positions[d.from].y; })
            .attr("y2", function (d) { return positions[d.to].y; })
            .attr("stroke-width", 1)
            .attr("stroke", "black");

        const redrawlines = () => {
            svg.selectAll("line").data(dependencies)
                .attr("x1", function (d) { return positions[d.from].x; })
                .attr("x2", function (d) { return positions[d.to].x; })
                .attr("y1", function (d) { return positions[d.from].y; })
                .attr("y2", function (d) { return positions[d.to].y; })
                .attr("stroke-width", 1)
                .attr("stroke", "black");

        };

        var div = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);

        var comp = comps.enter()
            .append("g")
            .attr("id", function(d) { return "comp-" + d.name })
            .attr("class", "node-group")
            .attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")"
            })
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended))
            .on("mouseenter", function(d) {
                div.transition().duration(200).style("opacity", .9);
                div.html(d.name + "<br/><hr> with some short description text of "  + d.name + " service")
                //.style("left", (d3.event.pageX) + "px")
                //.style("top", (d3.event.pageY - 28) + "px");
                    .style("left", (d.x + 5) + "px")
                    .style("top", (d.y + 5) + "px");
            })
            .on("mouseleave", function(d) {
                div.transition()
                    .duration(500)
                    .style("opacity", 0);
            });

        comp.append("circle")
            .attr("r", function(d) { return 30/*d.r*/; })
            .attr("stroke", function(d) { return "yellow"/*d.color*/; })
            .attr("fill", "lightblue");

        comp.append("text")
            .text(function(d) { return d.name })
            .attr("x", 0)
            .attr("dy", ".35em")
            .attr("text-anchor", "middle")

        function dragstarted(d) {
            //console.info("drag start", d, this);
            d.dragging = true;
            d3.select(this).select("circle").attr("fill", "#8cccef");
            d3.select(this).interrupt().raise().classed("active", true);
            d3.event.sourceEvent.stopPropagation();
        }

        function dragged(d) {
            //console.info("drag", d3.event.x, d3.event.y, d, this, d3.select(this));
            d.x = d3.event.x;
            d.y = d3.event.y;
            redrawlines();
            d3.select(this).attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")"
            });
        }

        function dragended(d) {
            //console.info("drag end", d, this);
            d.dragging = false;
            d3.select(this).classed("active", false);
            d3.select(this).select("circle").attr("fill", "lightblue");
        }
    }
    render() {
        return (
            <div>
                <Toggle toggled={this.state.active} onToggleChanged={value => {this.setState({active: value})}} />
                <br/>
                <svg ref={node => this.node = node} width="960" height="450"></svg>
            </div>
        );
    }
}

export {Diagram};