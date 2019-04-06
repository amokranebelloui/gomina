import React from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import {ArchiDiagram} from "../archidiagram/ArchiDiagram";
import {DSM} from "../dependency/DSM";
import {Dependencies} from "../dependency/Dependencies";
import {TagCloud} from "../common/TagCloud";
import {Container} from "../common/Container";

class DependenciesApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            systems: [],
            components: [],
            dependencies: [],
            selectedSystems: [],
            selectedFunctionTypes: [],
            selectedDependencies: [],

            deps: {services: [], functionTypes: [], dependencies: []}
        };
    }

    componentDidMount() {
        this.retrieveSystems();
        this.retrieveDependencies();
    }
    retrieveSystems() {
        axios.get('/data/components/systems')
            .then(response => {
                console.log("systems data", response.data);
                this.setState({systems: response.data});
            })
            .catch(function (error) {
                console.log("systems data error", error);
            });
    }
    retrieveDependencies(systems, functionTypes) {
        axios.get('/data/dependencies?systems=' + (systems || this.state.selectedSystems) + '&functionTypes=' + (functionTypes || this.state.selectedFunctionTypes) )
            .then(response => {
                console.log("dependencies data", response.data);
                this.setState({deps: response.data});
            })
            .catch(function (error) {
                console.log("dependencies data error", error);
            });
    }
    reloadSources() {
        const thisComponent = this;
        axios.post('/data/dependencies/reload')
            .then(() => thisComponent.retrieveDependencies())
            .catch((error) => console.log("dependencies data error", error));
    }
    selectedSystemsChanged(systems) {
        console.info("selected systems", systems);
        this.setState({selectedSystems: systems});
        this.retrieveDependencies(systems, this.state.selectedFunctionTypes)
    }
    selectedFunctionTypesChanged(functionTypes) {
        console.info("selected functionTypes", functionTypes);
        this.setState({selectedFunctionTypes: functionTypes});
        this.retrieveDependencies(this.state.selectedSystems, functionTypes)
    }

    onSelectedDependenciesChanged(selectedDeps) {
        this.setState({selectedDependencies: selectedDeps})
    }
    render() {
        const selectedDeps = this.state.deps.dependencies.filter(d => this.state.selectedDependencies.find(s => s.from === d.from && s.to === d.to));
        return (
            <AppLayout title="Architecture Diagram">
                <PrimarySecondaryLayout>
                    <Container>
                        <DSM components={this.state.deps.services}
                             dependencies={this.state.deps.dependencies.map(d => {return {"from": d.from, "to": d.to, "count": d.functions.length, "detail": d.functions}})}
                             onSelectedDependenciesChanged={e => this.onSelectedDependenciesChanged(e)}  />
                    </Container>
                    <div>
                        <h3>Filter:</h3>
                        <button onClick={e => this.retrieveDependencies()}>Refresh</button>
                        <button onClick={() => this.reloadSources()}>Reload Sources</button>
                        <br/>
                        <TagCloud tags={this.state.systems} selectionChanged={e => this.selectedSystemsChanged(e)} />
                        <br/>
                        <TagCloud tags={this.state.deps.functionTypes} selectionChanged={e => this.selectedFunctionTypesChanged(e)} />
                        <hr/>
                        <h3>Dependency Details</h3>
                        {selectedDeps && selectedDeps.length > 0
                            ? <Dependencies dependencies={selectedDeps} />
                            : <span>Select some dependencies to see the details</span>
                        }

                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}

export { DependenciesApp };