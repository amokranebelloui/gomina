import React from "react";
import ReactDOM from "react-dom";
import {createBrowserHistory} from "history";
import {Route, Router, Switch} from "react-router";
import {Index} from "./app/Index";
import {EnvApp} from "./app/EnvApp";
import {ArchitectureApp} from "./app/ArchitectureApp";
import {PipelineApp} from "./app/PipelineApp";
import {ComponentApp} from "./app/ComponentApp";
import {SandboxApp} from "./app/SandboxApp";
import "./application.css"
import {HostsApp} from "./app/HostsApp";
import {ComponentsApp} from "./app/ComponentsApp";
import {WorkApp} from "./app/WorkApp";
import {DependenciesApp} from "./app/DependenciesApp";
import {UserApp} from "./app/UserApp";
import {LibrariesApp} from "./app/LibrariesApp";

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
                <Route path="/components" render={props => <ComponentsApp {...props} />}/>
                <Route path="/component/:id/doc/:docId" render={props => <ComponentApp {...props} />}/>
                <Route path="/component/:id/scm" render={props => <ComponentApp {...props} />}/>
                <Route path="/component/:id" render={props => <ComponentApp {...props} />}/>
                <Route path="/library/:id" component={LibrariesApp}/>
                <Route path="/libraries" component={LibrariesApp}/>
                <Route path="/dependencies" component={DependenciesApp}/>
                <Route path="/architecture/:id" render={props => <ArchitectureApp {...props} />}/>
                <Route path="/architecture" component={ArchitectureApp}/>
                <Route path="/hosts" render={props => <HostsApp {...props} />}/>
                <Route path="/host/:id" render={props => <HostsApp {...props} />}/>
                <Route path="/envs/:id/svc/:svcId/:instanceId" render={props => <EnvApp {...props} />}/>
                <Route path="/envs/:id/svc/:svcId" render={props => <EnvApp {...props} />}/>
                <Route path="/envs/:id" render={props => <EnvApp {...props} />}/>
                <Route path="/envs" render={props => <EnvApp {...props} />}/>
                <Route path="/pipeline" render={props => <PipelineApp {...props} />}/>
                <Route path="/work/:id" render={props => <WorkApp {...props} />}/>
                <Route path="/work" render={props => <WorkApp {...props} />}/>
                <Route path="/user/:id" render={props => <UserApp {...props} />}/>
                <Route path="/user" render={props => <UserApp {...props} />}/>
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
