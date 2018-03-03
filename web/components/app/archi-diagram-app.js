import React from "react";
import axios from "axios/index";
import {AppLayout} from "./common/layout";
import {Diagram} from "./archidiagram/archi-diagram";

class ArchiDiagramApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {components: [], dependencies: []};
        this.addData = this.addData.bind(this);
        this.removeData = this.removeData.bind(this);
        this.componentMoved = this.componentMoved.bind(this);
    }

    componentDidMount() {
        axios.get('/data/diagram/data')
            .then(response => {
                console.log("diagram data", response.data);
                this.setState({components: response.data.components, dependencies: response.data.dependencies});
                console.info("state ", this.state)
            })
            .catch(function (error) {
                console.log("diagram data error", error.response);
            });
    }

    addData() {
        //const a = Math.floor((Math.random() * 10));
        //const b = Math.floor((Math.random() * 10));
        //const newDeps = [].concat(this.state.dependencies, [{from: a, to: b}]);
        //this.setState({dependencies: dependencies});
        console.info("Reload: add");
    }

    removeData() {
        //this.state.dependencies.pop();
        //const newDeps = this.state.dependencies;
        //this.setState({dependencies: dependencies2});
        console.info("Reload: rem");
    }

    componentMoved(d) {
        console.info("moved 2", d);

        axios.post('/data/diagram/update', d)
            .then(response => {
                console.log("reloaded", response.data);
            })
            .catch(function (error) {
                console.log("reload error", error.response);
            });
    }

    render() {
        return (
            <AppLayout title="Architecture Diagram">
                <button onClick={this.addData}>Add</button>
                <button onClick={this.removeData}>Remove</button>
                <Diagram components={this.state.components}
                         dependencies={this.state.dependencies}
                         onLinkSelected={d => console.info("selected", d)}
                         onComponentMoved={this.componentMoved}/>
            </AppLayout>
        );
    }
}

export { ArchiDiagramApp };