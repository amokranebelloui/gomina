import React from "react";
import ReactDOM from "react-dom";
import {createBrowserHistory} from "history";
import {Router, Switch, Route} from "react-router";
import {BrowserRouter, Link, NavLink} from "react-router-dom";
import {ArchiDiagramApp, EnvApp, PipelineApp, ProjectsApp, ProjectApp, SandboxApp, Index, About} from "./components";
import {posts} from "./data";
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
            <div>
                <Link to="/">index</Link> -&nbsp;
                <Link to="/archi">archi</Link> -&nbsp;
                <Link to="/envs">envs</Link> -&nbsp;
                <Link to="/pipeline">pipeline</Link> -&nbsp;
                <Link to="/projects">projects</Link> -&nbsp;
                <Link to="/project/basket">project1</Link> -&nbsp;
                <Link to="/project/order">project2</Link> -&nbsp;
                <Link to="/about">about</Link> -&nbsp;
                <Link to="/about/detail/amokrane">about amokrane</Link> -&nbsp;
                <Link to="/about/detail/jim">about jim</Link> -&nbsp;
                <Link to="/sandbox">sandbox</Link> -&nbsp;
                <Link to="/unknown">unknown page</Link>&nbsp;
                <br/>

                <Switch>
                    <Route exact path="/" component={Index}/>
                    <Route path="/archi" component={ArchiDiagramApp}/>
                    <Route path="/envs" component={() => <EnvApp instances={this.state.instances} />}/>
                    <Route path="/pipeline" component={() => <PipelineApp instances={this.state.instances} projects={this.state.projects} />}/>
                    <Route path="/projects" component={() => <ProjectsApp projects={this.state.projects} />}/>
                    <Route path="/project/:id" component={props => <ProjectApp instances={this.state.instances} project={this.state.projects[0]} {...props} />}/>
                    <Route path="/about" component={About}/>
                    <Route path="/sandbox" component={() => <SandboxApp posts={posts}/>}/>
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
