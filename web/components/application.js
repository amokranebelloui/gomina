
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
                <Link to="/">index</Link> -&nbsp;
                <Link to="/archi">archi</Link> -&nbsp;
                <Link to="/envs">envs</Link> -&nbsp;
                <Link to="/pipeline">pipeline</Link> -&nbsp;
                <Link to="/projects">projects</Link> -&nbsp;
                <Link to="/project">project</Link> -&nbsp;
                <Link to="/about">about</Link> -&nbsp;
                <Link to="/about/detail/amokrane">about amokrane</Link> -&nbsp;
                <Link to="/about/detail/jim">about jim</Link> -&nbsp;
                <Link to="/sandbox">sandbox</Link> -&nbsp;
                <Link to="/unknown">unknown page</Link>&nbsp;
                <br/>

                <Switch>
                    <Route exact path="/" component={Index}/>
                    <Route path="/archi" component={ArchiDiagramApp}/>
                    <Route path="/envs" component={() => <EnvApp instances={allInstances}/>}/>
                    <Route path="/pipeline" component={PipelineApp}/>
                    <Route path="/projects" component={ProjectsApp}/>
                    <Route path="/project" component={ProjectApp}/>
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
