
class Diagram extends React.Component {
    constructor(props){
        super(props);
        this.createDiagram = this.createDiagram.bind(this)
        this.state = {active: true}
    }
    componentDidMount() {
        console.info("mounted")
        this.createDiagram()
    }
    componentDidUpdate() {
        console.info("updated")
        this.createDiagram()
    }
    componentWillUnmount() {
        console.info("destroy")
        var el = this.getDOMNode();
        d3.destroy(el);
    }
    createDiagram() {
        //const svg = this.node;
        const positions = this.props.components;
        const dependencies = this.props.dependencies;
        var svg = d3.select("svg");

        console.info(svg);
        var comps = svg.selectAll("g node-group").data(positions)

        svg.selectAll("line").data(dependencies).enter()
            .append("line")
            .attr("x1", function (d) { console.log(positions, d); return positions[d.from].x; })
            .attr("x2", function (d) { console.log(positions, d); return positions[d.to].x; })
            .attr("y1", function (d) { console.log(positions, d); return positions[d.from].y; })
            .attr("y2", function (d) { console.log(positions, d); return positions[d.to].y; })
            .attr("stroke-width", 1)
            .attr("stroke", "black");

        var div = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);


        var comp = comps.enter()
            .append("g")
            .attr("class", "node-group")
            .attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")"
            })
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
            .attr("fill", "lightblue")

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
                {this.state.active && <svg ref={node => this.node = node} width="960" height="450"></svg>}
            </div>
        );
    }
}
