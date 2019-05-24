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
    onComponentSelected: DiagramComponentType => void,
    onLinkSelected: DiagramDependencyType => void
}

type State = {
    active: boolean
}

class ArchiDiagram extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            active: true
        }
    }
    componentDidMount() {
        console.info("?didMount");
        this.drawNodes(this.props);
        this.redrawLines(this.props, true);
    }
    componentWillReceiveProps(nextProps: Props) {
        console.info("?receiveProps");
        this.drawNodes(nextProps); // FIXME Moyen update existing
        this.redrawLines(nextProps, false);
    }
    componentWillUnmount() {
        //$FlowFixMe
        const el = this.node;
        //d3.destroy(el); // Doesn't work
        //d3.select("svg").remove(); // Is it necessary?
    }
    dragStarted(d: DiagramComponentType2, n: any) {
        d.dragging = true;
        d3.select(n).select("circle").attr("fill", "#8cccef");
        d3.select(n).interrupt().raise().classed("active", true);
        d3.event.sourceEvent.stopPropagation();
        this.props.onComponentSelected && this.props.onComponentSelected(d)
    }
    dragged(d: DiagramComponentType2, n: any) {
        d.x = d3.event.x;
        d.y = d3.event.y;
        this.redrawLines(this.props);
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
    onComponentSelected(c: DiagramComponentType) {
        this.props.onComponentSelected && this.props.onComponentSelected(c)
    }
    onLinkSelected(d: DiagramDependencyType) {
        this.props.onLinkSelected && this.props.onLinkSelected(d)
    }
    drawNodes(props: Props) {
        const this_ = this;
        const positions = props.components;
        console.info("?draw", positions);
        //$FlowFixMe
        var svg = d3.select(this.node);

        //svg.append("g").attr("id", "links");
        //svg.append("g").attr("id", "nodes");

        //$FlowFixMe
        var div = d3.select(this.container).append("div")
            .attr("class", "tooltip")
            .style("position", "absolute")
            .style("opacity", 0);

        var comps = svg.select("#nodes").selectAll("g.node-group").data(positions, d => "comp-" + d.name);


        comps.exit()
            .remove();

        comps.transition()
            .duration(100)
            .attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")"
            });


        var comp = comps.enter()
            .append("g")
            //.attr("id", d => "comp-" + d.name)
            .attr("class", "node-group")
            .attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")"
            })
            .on("click", d => {this_.onComponentSelected(d)})
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
    redrawLines(props: Props, init: boolean = false) {
        const this_ = this;
        let indexedComponents = index(props.components);
        //$FlowFixMe
        const svg = d3.select(this.node);
        const dependencies = svg.select("#links").selectAll("line").data(props.dependencies, d => d.from + '|' + d.to);

        dependencies.exit()
            .transition()
            .duration(100)
            .style("opacity", "0")
            .remove();

        const line = dependencies.enter().append("line")
            .call(this.dependency, indexedComponents)
            .on("click", d => this_.onLinkSelected(d));
        if (init) {
            line.attr("stroke", "orange");
        }
        else {
            line.attr("stroke", "red")
                .style("opacity", "0")
                .transition()
                .duration(100)
                .attr("stroke", "orange")
                .style("opacity", "1");
        }

        dependencies.call(this.dependency, indexedComponents)
            .attr("stroke", "orange");
    };
    dependency(selection: any, positions: DiagramComponentIndexType) {
        selection.attr("x1", function (d) { return positions[d.from].x; })
            .attr("x2", function (d) { return positions[d.to].x; })
            .attr("y1", function (d) { return positions[d.from].y; })
            .attr("y2", function (d) { return positions[d.to].y; })
            .attr("stroke-width", 3);
    }
    
    render() {
        return (
            <div ref={node =>
                //$FlowFixMe
                this.container = node} style={{
                    position: 'relative',
                    border: '1px solid lightgray',
                    minWidth:  '400px', minHeight: '450px', overflow: 'auto'
                }}
            >
                <svg ref={node =>
                    //$FlowFixMe
                    this.node = node} width="100%" height="450">
                    <g id="links"></g>
                    <g id="nodes"></g>
                </svg>
            </div>
        );
    }
}

export {ArchiDiagram};
export type {DiagramComponentType, DiagramDependencyType}