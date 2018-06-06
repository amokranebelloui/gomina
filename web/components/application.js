import React from "react";
import ReactDOM from "react-dom";
import {createBrowserHistory} from "history";
import {Route, Router, Switch} from "react-router";
import {Index} from "./app/index-app";
import {EnvApp} from "./app/env-app";
import {ArchiDiagramApp} from "./app/archi-diagram-app";
import {PipelineApp} from "./app/pipeline-app";
import {ProjectApp} from "./app/project-app";
import {SandboxApp} from "./app/sandbox-app";
import "./application.css"
import {HostsApp} from "./app/HostsApp";

const history = createBrowserHistory();

class Blocker extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        console.info('blocker');
        //this.props.location.pathname
        return (
            <Switch>
                <Route exact path="/" component={Index}/>
                <Route path="/archi" component={ArchiDiagramApp}/>
                <Route path="/hosts" render={props => <HostsApp {...props} />}/>
                <Route path="/host/:id" render={props => <HostsApp {...props} />}/>
                <Route path="/envs/:id" render={props => <EnvApp {...props} />}/>
                <Route path="/envs" render={props => <EnvApp {...props} />}/>
                <Route path="/pipeline" render={props => <PipelineApp {...props} />}/>
                <Route path="/projects" render={props => <ProjectApp {...props} />}/>
                <Route path="/project/:id/doc/:docId" render={props => <ProjectApp {...props} />}/>
                <Route path="/project/:id" render={props => <ProjectApp {...props} />}/>
                <Route path="/sandbox" component={() => <SandboxApp />}/>
                <Route path="*" component={() => <div>I don't know you man!!!</div>} />
            </Switch>
        );
    }
}

class RouterApplication extends React.Component {
    render() {
        // Wrapping in the Route injects the location -> Blocker Component and forces a refresh
        console.info('application');
        return (
            <Router path="/app" history={history}>
                <Route component={Blocker} />
            </Router>
        );
    }
}

//console.info(document.getElementById('root'))
ReactDOM.render(<RouterApplication />, document.getElementById('root'));
