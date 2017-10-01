import React from "react";
import {Router, Switch, Route} from "react-router";
import SockJS from "sockjs-client";
import {groupBy, Toggle} from "./gomina";
import {AppLayout} from "./layout";
import {Diagram} from "./archi-diagram";
import {EnvironmentLogical} from "./env";
import {ProjectPipeline} from "./pipeline";
import {ProjectSummary, ScmLog} from "./project";
import {MyComponent, Toggle2, LoggingButton, Posts, WarningBanner} from "./sandbox";
import {sampleCommits} from "./data";
import C1 from "./module.js";

var components = [
    {name: "referential", x:400, y:350},
    {name: "basket", x:100, y:200},
    {name: "order", x:150, y:120},
    {name: "market", x:200, y:200},
    {name: "cross", x:300, y:100},
    {name: "fixout", x:300, y:150},
    {name: "emma", x:300, y:200},
    {name: "fidessa", x:150, y:50},
    {name: "pie", x:100, y:50},
    {name: "pita", x:50, y:50},
    {name: "fixin", x:50, y:150},
    {name: "audit", x:150, y:350},
    {name: "mis", x:200, y:350},
];
var dependencies = [
    {from:1, to:2}, // basket, order
    {from:3, to:1}, // market, order
];

class ArchiDiagramApp extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        return (
            <AppLayout title="Architecture Diagram">
                <Diagram components={components} dependencies={dependencies} />
            </AppLayout>
        );
    }
}

class EnvApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {env: 'PROD', realtime: false, instances: this.props.instances}
        this.connect = this.connect.bind(this)
        this.switch = this.switch.bind(this)
    }
    connect() {
        this.sock = new SockJS('/realtime');

        const thisComponent = this;
        var eventsDiv = this.eventsList;
        this.sock.onopen = function() {
            console.log('open');
            thisComponent.setState({realtime: true});
            eventsDiv.innerHTML = eventsDiv.innerHTML + 'Open<br>';
            //sock.send('test');
        };

        this.sock.onmessage = function(e) {
            //var r = Math.floor((Math.random() * 3) + 1);
            //var fakeStatus = r == 1 ? 'LIVE' : r == 2 ? 'LOADING' : 'DOWN';
            let event = JSON.parse(e.data);
            //console.log('message', event);
            eventsDiv.innerHTML = eventsDiv.innerHTML + ' ' + e.data + '<br>';

            // FIXME review how to identify instances
            const found = thisComponent.state.instances.find(instance => instance.name == event.id);

            //console.info("found", found);

            if (found) {
                found.status = event.status;
                thisComponent.setState({instances: thisComponent.state.instances});
            }
        };

        this.sock.onclose = function() {
            console.log('close');
            thisComponent.setState({realtime: false});
            eventsDiv.innerHTML = eventsDiv.innerHTML + 'Close<br>';
        };
    }
    switch(newStatus) {
        if (this.state != newStatus) {
            if (newStatus) {
                this.connect();
            }
            else {
                this.sock.close();
            }
        }
    }
    componentDidMount() {
        //this.connect();
    }
    selectEnv(env) {
        this.setState({env: env});
    }
    render() {
        const instancesByEnv = groupBy(this.state.instances, 'env');
        const instances = instancesByEnv[this.state.env] || [];
        const envs = Object.keys(instancesByEnv);
        console.info('envs', envs);

        return (
            <AppLayout title={"Environment '" + this.state.env + "'"}>
                <div style={{float: 'right'}}>
                    <Toggle toggled={this.state.realtime} onToggleChanged={this.switch} />
                    <div ref={node => this.eventsList = node}></div>
                </div>
                {envs.map(env =>
                    <button key={env} style={{color: this.state.env == env ? 'gray' : null}} onClick={e => this.selectEnv(env)}>{env}</button>
                )}
                <hr/>
                <EnvironmentLogical instances={instances} />
            </AppLayout>
        );
    }
}

class PipelineApp extends React.Component {
    render() {
        const instancesByProject = groupBy(this.props.instances, 'project');
        console.info('instances by project', instancesByProject);
        const projects = this.props.projects;
        return (
            <AppLayout title="Pipeline">
                {projects.map(project =>
                    <ProjectPipeline key={project.id} project={project} instances={instancesByProject[project.id]} />
                )}
            </AppLayout>
        );
    }
}

class ProjectsApp extends React.Component {
    render() {
        const projects = this.props.projects;
        return (
            <AppLayout title="Projects">
                {projects.map(project =>
                    <ProjectSummary key={project.id} project={project} />
                )}
            </AppLayout>
        );
    }
}

class ProjectApp extends React.Component {
    render() {
        const project = this.props.project;
        const commits = sampleCommits;
        const instances = this.props.instances.filter(instance => instance.project == project.id);
        const title = "Project '" + project.label + "'";
        return (
            <AppLayout title={title}>
                <ScmLog key={project.id} project={project} commits={commits} instances={instances} />
            </AppLayout>
        );
    }
}

class SandboxApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {logged: true, user: "amo"}
    }
    render() {
        let status;
        if (this.state.logged) {
            status = <span>Hello '{this.state.user}'</span>
        }
        else {
            status = <span>Hi buddy</span>
        }
        return (
            <AppLayout title='Sandbox'>
                <span>{status}</span>&nbsp;
                {this.state.logged && <span>Logged in</span>}
                <br/>

                Linked toggles
                <Toggle toggled={this.state.logged} onToggleChanged={e => {this.setState({logged: e})}} />
                <Toggle toggled={this.state.logged} onToggleChanged={e => {this.setState({logged: e})}} />
                <br/>
                Independent <Toggle2 />
                <C1 />
                <LoggingButton/>
                <WarningBanner warn="Something happened" />
                <WarningBanner warn="" />
                <br/>
                <Posts posts={this.props.posts} />
                <MyComponent label="hello world" highlighted={true} />
                <MyComponent label="hello world" />
            </AppLayout>
        );
    }
}

function Index(props) {
    console.info('index');
    return (
        <AppLayout title='Index'>
            Index
        </AppLayout>
    );
}

function About(props) {
    console.info('about');
    return (
        <AppLayout title='About'>
            <Switch>
                <Route path="/about/detail/:id" component={About2}/>
                <Route path="/about" component={About1}/>
            </Switch>
        </AppLayout>
    );
}

function About1(props) {
    console.info('about1');
    return (
        <div>
            About1
        </div>
    );
}

function About2(props) {
    console.info('about2', props.match);
    return (
        <div>
            About2
            <br />
            {props.match.params.id}
        </div>
    );
}

export {ArchiDiagramApp, EnvApp, PipelineApp, ProjectsApp, ProjectApp, SandboxApp, Index, About, About1, About2};

