
import React from 'react';
import ReactDOM from 'react-dom';
import {createBrowserHistory} from 'history';

import { Router, Switch, Route } from 'react-router'
import { BrowserRouter, Link, NavLink } from 'react-router-dom'

import {ArchiDiagramApp, EnvApp, PipelineApp, ProjectsApp, ProjectApp, SandboxApp, Index, About} from './components';
import {allInstances, allProjects, sampleCommits, posts} from './data'

const history = createBrowserHistory();

class Blocker extends React.Component {
    render() {
        console.info('blocker');
        return (
            <div>
                <Link to="/app/">index</Link> -&nbsp;
                <Link to="/app/archi">archi</Link> -&nbsp;
                <Link to="/app/envs">envs</Link> -&nbsp;
                <Link to="/app/pipeline">pipeline</Link> -&nbsp;
                <Link to="/app/projects">projects</Link> -&nbsp;
                <Link to="/app/project">project</Link> -&nbsp;
                <Link to="/app/about">about</Link> -&nbsp;
                <Link to="/app/about/detail/amokrane">about amokrane</Link> -&nbsp;
                <Link to="/app/about/detail/jim">about jim</Link> -&nbsp;
                <Link to="/app/sandbox">sandbox</Link> -&nbsp;
                <Link to="/app/unknown">unknown page</Link>&nbsp;
                <br/>
                {this.props.location.pathname} *
                <hr/>

                <Switch>
                    <Route exact path="/app" component={Index}/>
                    <Route path="/app/archi" component={ArchiDiagramApp}/>
                    <Route path="/app/envs" component={() => <EnvApp instances={allInstances}/>}/>
                    <Route path="/app/pipeline" component={PipelineApp}/>
                    <Route path="/app/projects" component={ProjectsApp}/>
                    <Route path="/app/project" component={ProjectApp}/>
                    <Route path="/app/about" component={About}/>
                    <Route path="/app/sandbox" component={() => <SandboxApp posts={posts}/>}/>
                    <Route path="*" component={() => <div>I don't know you man!!!</div>} />
                </Switch>
                <hr/>
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
