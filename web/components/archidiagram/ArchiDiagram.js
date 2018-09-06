import React from "react";
import * as d3 from "d3";
import {Toggle} from "../common/component-library";
import "./ArchiDiagram.css";

function index(list) {
    return list.reduce((result, obj) => {
        result[obj['name']] = obj;
        return result;
    }, {});
}

class ArchiDiagram extends React.Component {
    constructor(props){
        super(props);
        this.createDiagram = this.createDiagram.bind(this);
        this.redrawLines = this.redrawLines.bind(this);
        this.onLinkSelected = this.onLinkSelected.bind(this);
        //this.onComponentMoved = this.onComponentMoved.bind(this)
        this.state = {active: true}
    }
    componentDidMount() {
        this.createDiagram();
        //console.info("did mount", this.createDiagram)
    }
    componentDidUpdate() {
        this.createDiagram(); // FIXME Moyen update existing
        // update ?
        //console.info("diagram prop updated", this.createDiagram)
        this.redrawLines(false);
    }
    componentWillUnmount() {
        const el = this.node;
        console.info(el);
        //d3.destroy(el); // Doesn't work
        //d3.select("svg").remove(); // Is it necessary?
    }
    dependency(selection, positions) {
        selection.attr("x1", function (d) { return positions[d.from].x; })
            .attr("x2", function (d) { return positions[d.to].x; })
            .attr("y1", function (d) { return positions[d.from].y; })
            .attr("y2", function (d) { return positions[d.to].y; })
            .attr("stroke-width", 3);
    }

    redrawLines(init) {
        const this_ = this;
        let indexedComponents = index(this.props.components);
        const svg = d3.select(this.node);
        const dependencies = svg.select("#links").selectAll("line").data(this.props.dependencies, d => {return d.from+'|'+d.to});

        dependencies.exit()
            .transition()
            .duration(1000)
            .style("opacity", "0")
            .remove();

        const line = dependencies.enter().append("line")
            .call(this.dependency, indexedComponents)
            .on("click", function(d) {
                console.info("selected dependency", d);
                this_.onLinkSelected(d)
            });
        if (init) {
            line.attr("stroke", "orange");
        }
        else {
            line.attr("stroke", "red")
                .style("opacity", "0")
                .transition()
                .duration(1000)
                .attr("stroke", "orange")
                .style("opacity", "1");
        }

        dependencies.call(this.dependency, indexedComponents)
            .attr("stroke", "orange");
    };
    dragStarted(d, comp) {
        //console.info("drag start", d, this);
        d.dragging = true;
        d3.select(comp).select("circle").attr("fill", "#8cccef");
        d3.select(comp).interrupt().raise().classed("active", true);
        d3.event.sourceEvent.stopPropagation();
    }
    dragged(d, comp) {
        //console.info("drag", this);
        d.x = d3.event.x;
        d.y = d3.event.y;
        this.redrawLines();
        d3.select(comp).attr("transform", function(d) {
            return "translate(" + d.x + "," + d.y + ")"
        });
    }
    dragEnded(d, comp) {
        //console.info("drag end", d, this);
        d.dragging = false;
        d3.select(comp).classed("active", false);
        d3.select(comp).select("circle").attr("fill", "lightblue");
        this.onComponentMoved(d)
    }
    onComponentMoved(d) {
        this.props.onComponentMoved && this.props.onComponentMoved(d)
    }
    onLinkSelected(d) {
        this.props.onLinkSelected && this.props.onLinkSelected(d)
    }
    createDiagram() {
        const this_ = this;
        const positions = this.props.components;
        console.info('$$$$2', positions);
        var svg = d3.select(this.node);

        //svg.append("g").attr("id", "links");
        //svg.append("g").attr("id", "nodes");

        this.redrawLines(true);

        var div = d3.select(this.container).append("div")
            .attr("class", "tooltip")
            .style("position", "absolute")
            .style("opacity", 0);

        var comps = svg.select("#nodes").selectAll("g node-group").data(positions, d => {return d.name});

        var comp = comps.enter()
            .append("g")
            .attr("id", function(d) { return "comp-" + d.name })
            .attr("class", "node-group")
            .attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")"
            })
            .call(d3.drag()
                .on("start", function(d) { this_.dragStarted(d, this)})
                .on("drag", function(d) { this_.dragged(d, this)})
                .on("end", function(d) { this_.dragEnded(d, this)})
            )
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

    }
    render() {
        return (
            <div>
                <Toggle toggled={this.state.active} onToggleChanged={value => {this.setState({active: value})}} />
                <br/>
                <div ref={node => this.container = node} style={{position: 'relative'}}>
                    <svg ref={node => this.node = node} width="960" height="450">
                        <g id="links"></g>
                        <g id="nodes"></g>
                    </svg>
                </div>
            </div>
        );
    }
}

export {ArchiDiagram};