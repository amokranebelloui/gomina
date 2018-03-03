import React from "react";
import ReactDOM from "react-dom";
import {createBrowserHistory} from "history";
import {Route, Router, Switch} from "react-router";
import {Link} from "react-router-dom";
import {Index} from "./app/index-app";
import {EnvApp} from "./app/env-app";
import {ArchiDiagramApp} from "./app/archi-diagram-app";
import {PipelineApp} from "./app/pipeline-app";
import {ProjectsApp} from "./app/projects-app";
import {ProjectApp} from "./app/project-app";
import {SandboxApp} from "./app/sandbox-app";

const history = createBrowserHistory();

class Blocker extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        console.info('blocker');
        return (
            <div style={{fontFamily: 'Garamond'}}>
                <Link to="/">index</Link> -&nbsp;
                <Link to="/archi">archi</Link> -&nbsp;
                <Link to="/envs">envs</Link> -&nbsp;
                <Link to="/pipeline">pipeline</Link> -&nbsp;
                <Link to="/projects">projects</Link> -&nbsp;
                <Link to="/sandbox">sandbox</Link> -&nbsp;
                <Link to="/unknown">unknown page</Link>&nbsp;
                <br/>

                <Switch>
                    <Route exact path="/" component={Index}/>
                    <Route path="/archi" component={ArchiDiagramApp}/>
                    <Route path="/envs/:id" render={props => <EnvApp {...props} />}/>
                    <Route path="/envs" render={props => <EnvApp {...props} />}/>
                    <Route path="/pipeline" render={props => <PipelineApp {...props} />}/>
                    <Route path="/projects" render={props => <ProjectsApp {...props} />}/>
                    <Route path="/project/:id" render={props => <ProjectApp {...props} />}/>
                    <Route path="/sandbox" component={() => <SandboxApp />}/>
                    <Route path="*" component={() => <div>I don't know you man!!!</div>} />
                </Switch>
                <hr/>
                {this.props.location.pathname}
            </div>
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
