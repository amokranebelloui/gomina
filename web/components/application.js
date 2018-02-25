import React from "react";
import ReactDOM from "react-dom";
import {createBrowserHistory} from "history";
import {Router, Switch, Route} from "react-router";
import {BrowserRouter, Link, NavLink} from "react-router-dom";
import {ArchiDiagramApp, EnvApp, PipelineApp, ProjectsApp, ProjectApp, SandboxApp, Index} from "./components";
import axios from "axios";

const history = createBrowserHistory();

class Blocker extends React.Component {
    constructor(props) {
        super(props);
        this.state = {instances: [], projects: []};

        const thisComponent = this;
        axios.get('/data/instances')
            .then(response => {
                console.log("instances", response.data);
                thisComponent.setState({instances: response.data});
            })
            .catch(function (error) {
                console.log("error", error);
            });
        axios.get('/data/projects')
            .then(response => {
                console.log("projects", response.data);
                thisComponent.setState({projects: response.data});
            })
            .catch(function (error) {
                console.log("error", error);
            });

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
                    <Route path="/envs" render={props => <EnvApp instances={this.state.instances} {...props} />}/>
                    <Route path="/pipeline" component={() => <PipelineApp instances={this.state.instances} projects={this.state.projects} />}/>
                    <Route path="/projects" render={props => <ProjectsApp projects={this.state.projects} {...props} />}/>
                    <Route path="/project/:id" render={props => <ProjectApp projects={this.state.projects} instances={this.state.instances} {...props} />}/>
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

console.info(document.getElementById('root'))

ReactDOM.render(<RouterApplication />, document.getElementById('root'));
