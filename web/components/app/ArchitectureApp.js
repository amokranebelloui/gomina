import React from "react";
import axios from "axios/index";
import {AppLayout} from "./common/layout";
import {ArchiDiagram} from "../archidiagram/ArchiDiagram";
import {DSM} from "../dependency/DSM";

class ArchitectureApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            components: [],
            dependencies: [],

            deps: {projects: [], dependencies: []}
        };
        this.addData = this.addData.bind(this);
        this.removeData = this.removeData.bind(this);
        this.componentMoved = this.componentMoved.bind(this);
    }

    componentDidMount() {
        this.retrieveDiagram();
        this.retrieveDependencies();
    }
    retrieveDiagram() {
        axios.get('/data/diagram/data')
            .then(response => {
                console.log("diagram data", response.data);
                this.setState({components: response.data.components, dependencies: response.data.dependencies});
            })
            .catch(function (error) {
                console.log("diagram data error", error.response);
            });
    }
    retrieveDependencies() {
        axios.get('/data/dependencies')
            .then(response => {
                console.log("dependencies data", response.data);
                this.setState({deps: response.data});
            })
            .catch(function (error) {
                console.log("dependencies data error", error);
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
                <div style={{width: '100%', verticalAlign: 'top'}}>
                    <div style={{width: '40%', display: "inline-block", verticalAlign: 'top'}}>
                        <button onClick={e => this.retrieveDependencies()}>Refresh</button>
                        <DSM components={this.state.deps.projects}
                             dependencies={this.state.deps.dependencies.map(d => {return {"from": d.from, "to": d.to, "count": d.functions.length, "detail": d.functions}})} />
                    </div>
                    <div style={{width: '50%', boxSizing: 'border-box', display: "inline-block", verticalAlign: 'top'}}>
                        <button onClick={this.addData}>Add</button>
                        <button onClick={this.removeData}>Remove</button>
                        <ArchiDiagram components={this.state.components}
                                      dependencies={this.state.dependencies}
                                      onLinkSelected={d => console.info("selected", d)}
                                      onComponentMoved={this.componentMoved}/>
                    </div>

                </div>

            </AppLayout>
        );
    }
}

export { ArchitectureApp };