import React, {Fragment} from "react";
import axios from "axios/index";
import {DSM} from "../dependency/DSM";
import {Dependencies} from "../dependency/Dependencies";
import {TagCloud} from "../common/TagCloud";
import ls from "local-storage"
import {Well} from "../common/Well";
import {joinTags, splitTags} from "../common/utils";
import {ApplicationLayout} from "./common/ApplicationLayout";

class DependenciesApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            systems: [],
            workRefs: [],
            components: [],
            dependencies: [],
            selectedSystems: splitTags(ls.get('components.systems')),
            selectedWork: ls.get('components.selected.work'),
            selectedFunctionTypes: splitTags(ls.get('components.function.types')),
            selectedDependencies: [],

            deps: {services: [], functionTypes: [], dependencies: []}
        };
    }

    componentDidMount() {
        this.retrieveSystems();
        this.retrieveWorkRefs();
        this.retrieveDependencies();
    }
    retrieveSystems() {
        axios.get('/data/systems/refs')
            .then(response => this.setState({systems: response.data}))
            .catch((error) => console.log("systems data error", error));
    }
    retrieveWorkRefs() {
        axios.get('/data/work/refs')
            .then(response => this.setState({workRefs: response.data}))
            .catch((error) => console.log("work refs data error", error));
    }
    retrieveDependencies() {
        this.retrieveDependenciesFor(this.state.selectedSystems, this.state.selectedWork, this.state.selectedFunctionTypes);
    }
    retrieveDependenciesFor(systems, workIds, functionTypes) {
        axios.get('/data/dependencies?systems=' + systems + '&workIds=' + workIds + '&functionTypes=' + functionTypes)
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
        this.retrieveDependenciesFor(systems, this.state.selectedWork, this.state.selectedFunctionTypes);
        ls.set('components.systems', joinTags(systems));
    }
    selectedWorkChanged(work) {
        console.info("selected work", work);
        this.setState({selectedWork: work});
        this.retrieveDependenciesFor(this.state.selectedSystems, work, this.state.selectedFunctionTypes);
        ls.set('components.selected.work', work);
    }
    selectedFunctionTypesChanged(functionTypes) {
        console.info("selected functionTypes", functionTypes);
        this.setState({selectedFunctionTypes: functionTypes});
        this.retrieveDependenciesFor(this.state.selectedSystems, this.state.selectedWork, functionTypes);
        ls.set('components.function.types', joinTags(functionTypes));
    }

    onSelectedDependenciesChanged(selectedDeps) {
        this.setState({selectedDependencies: selectedDeps})
    }
    render() {
        const selectedDeps = this.state.deps.dependencies.filter(d => this.state.selectedDependencies.find(s => s.from === d.from && s.to === d.to));
        return (
            <ApplicationLayout title="Architecture Diagram"
               main={() =>
                   <DSM components={this.state.deps.services}
                        dependencies={this.state.deps.dependencies.map(d => {return {"from": d.from, "to": d.to, "count": d.functions.length, "detail": d.functions}})}
                        onSelectedDependenciesChanged={e => this.onSelectedDependenciesChanged(e)}  />
               }
               sidePrimary={() =>
                   <Fragment>
                       <b>Systems:</b>
                       <TagCloud tags={this.state.systems} selected={this.state.selectedSystems}
                                 selectionChanged={e => this.selectedSystemsChanged(e)} />
                       <br/>
                       <b>Work: </b>
                       <select name="type" value={this.state.selectedWork}
                               onChange={e => this.selectedWorkChanged(e.target.value)}
                               style={{width: '150px', fontSize: 9}}>
                           <option value=""></option>
                           {this.state.workRefs.map(w =>
                               <option key={w.id} value={w.id}>{w.label} ({w.components.length})</option>
                           )}
                       </select>
                       <br/>
                       <b>Function Types:</b>
                       <TagCloud tags={this.state.deps.functionTypes} selected={this.state.selectedFunctionTypes}
                                 selectionChanged={e => this.selectedFunctionTypesChanged(e)} />
                       <hr/>
                       <button onClick={e => this.retrieveDependencies()}>Refresh</button>
                       <button onClick={() => this.reloadSources()}>Reload Sources</button>
                   </Fragment>
               }
               sideSecondary={() =>
                   <Fragment>
                       <h3>Dependency Details</h3>
                       {selectedDeps && selectedDeps.length > 0
                           ? <Dependencies dependencies={selectedDeps} />
                           : <span>Select some dependencies to see the details</span>
                       }
                   </Fragment>
               }
            />
        );
    }
}

export { DependenciesApp };