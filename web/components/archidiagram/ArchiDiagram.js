// @flow
import React from "react";
import * as d3 from "d3";
import "./ArchiDiagram.css";
import {Toggle} from "../common/Toggle";

function index(list) {
    return list.reduce((result, obj) => {
        result[obj['name']] = obj;
        return result;
    }, {});
}

type DiagramComponentType = {
    name: string,
    x: number,
    y: number
}

type DiagramComponentType2 = {
    name: string,
    x: number,
    y: number,
    dragging: boolean
}

type DiagramComponentIndexType = {
    [name: string]: DiagramComponentType,
}


type DiagramDependencyType = {
    from: string,
    to: string
}


type Props = {
    components: Array<DiagramComponentType>,
    dependencies: Array<DiagramDependencyType>,
    onComponentMoved: DiagramComponentType => void,
    onLinkSelected: DiagramDependencyType => void
}

type State = {
    active: boolean
}

class ArchiDiagram extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        //$FlowFixMe
        this.createDiagram = this.createDiagram.bind(this);
        //$FlowFixMe
        this.redrawLines = this.redrawLines.bind(this);
        //$FlowFixMe
        this.onLinkSelected = this.onLinkSelected.bind(this);
        //this.onComponentMoved = this.onComponentMoved.bind(this)

        this.state = {active: true}
    }
    componentDidMount() {
        this.createDiagram();
    }
    componentDidUpdate() {
        this.createDiagram(); // FIXME Moyen update existing
        // update ?
        this.redrawLines(false);
    }
    componentWillUnmount() {
        //$FlowFixMe
        const el = this.node;
        //d3.destroy(el); // Doesn't work
        //d3.select("svg").remove(); // Is it necessary?
    }
    dependency(selection: any, positions: DiagramComponentIndexType) {
        selection.attr("x1", function (d) { return positions[d.from].x; })
            .attr("x2", function (d) { return positions[d.to].x; })
            .attr("y1", function (d) { return positions[d.from].y; })
            .attr("y2", function (d) { return positions[d.to].y; })
            .attr("stroke-width", 3);
    }

    redrawLines(init: boolean = false) {
        const this_ = this;
        let indexedComponents = index(this.props.components);
        //$FlowFixMe
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
    dragStarted(d: DiagramComponentType2, n: any) {
        d.dragging = true;
        d3.select(n).select("circle").attr("fill", "#8cccef");
        d3.select(n).interrupt().raise().classed("active", true);
        d3.event.sourceEvent.stopPropagation();
    }
    dragged(d: DiagramComponentType2, n: any) {
        d.x = d3.event.x;
        d.y = d3.event.y;
        this.redrawLines();
        d3.select(n).attr("transform", function(d) {
            return "translate(" + d.x + "," + d.y + ")"
        });
    }
    dragEnded(d: DiagramComponentType2, n: any) {
        d.dragging = false;
        d3.select(n).classed("active", false);
        d3.select(n).select("circle").attr("fill", "lightblue");
        this.onComponentMoved(d)
    }
    onComponentMoved(d: DiagramComponentType) {
        this.props.onComponentMoved && this.props.onComponentMoved(d)
    }
    onLinkSelected(d: DiagramDependencyType) {
        this.props.onLinkSelected && this.props.onLinkSelected(d)
    }
    createDiagram() {
        const this_ = this;
        const positions = this.props.components;
        //$FlowFixMe
        var svg = d3.select(this.node);

        //svg.append("g").attr("id", "links");
        //svg.append("g").attr("id", "nodes");

        this.redrawLines(true);

        //$FlowFixMe
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
                <div ref={node =>
                         //$FlowFixMe
                         this.container = node} style={{position: 'relative'}}
                >
                    <svg ref={node =>
                        //$FlowFixMe
                        this.node = node} width="960" height="450">
                        <g id="links"></g>
                        <g id="nodes"></g>
                    </svg>
                </div>
            </div>
        );
    }
}

export {ArchiDiagram};
export type {DiagramComponentType, DiagramDependencyType}