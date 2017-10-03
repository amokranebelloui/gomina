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
import C1 from "./module.js";
import axios from "axios"

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
    constructor(props) {
        super(props);
        this.state = {project: {}};
        this.retrieveProject = this.retrieveProject.bind(this);
        console.info("!constructor ", this.props.match.params.id);
    }
    componentWillMount() {
        console.info("!mount ", this.props.match.params.id);
        this.retrieveProject(this.props.match.params.id);
    }
    componentWillReceiveProps(nextProps) {
        console.info("!props-chg ", this.props.match.params.id, nextProps.match.params.id);
        this.retrieveProject(nextProps.match.params.id);
    }
    retrieveProject(projectId) {
        const thisComponent = this;
        axios.get('/data/project/' + projectId)
            .then(response => {
                console.log("project", response.data);
                thisComponent.setState({project: response.data});
            })
            .catch(function (error) {
                console.log("error", error.response);
                thisComponent.setState({project: {}});
            });
    }
    render() {
        //console.info("!render ", this.props.match.params.id);
        const projects = this.props.projects;
        const project = this.state.project;
        const commits = project.commitLog || [];
        const instances = this.props.instances.filter(instance => instance.project == project.id);
        const title = "Project '" + project.label + "'";
        const cellStyle = {display: 'inline'};
        return (
            <AppLayout title={title}>
                <div style={{display: 'grid', gridGap: '2px', gridTemplateColumns: 'minmax(500px, 6fr) minmax(200px, 3fr)'}}>
                    <div style={cellStyle}>
                        {projects.map(project =>
                            <ProjectSummary key={project.id} project={project} />
                        )}
                    </div>
                    <div style={cellStyle}>
                        <ScmLog key={project.id} project={project} commits={commits} instances={instances} />
                    </div>
                </div>
            </AppLayout>
        );
    }
}

const posts = [
    {id: 1, title: 'Hello World 4', content: 'Welcome to learning React!'},
    {id: 2, title: 'Installation', content: 'You can install React from npm.'},
    {id: 3, title: 'Live Edit in Jetbrains, wo', content: 'You can live edit from Jetbrains IDE, wow!'},
    {id: 4, title: 'Scorpions, Tokyo Tapes', content: 'Kojo No Tsuki'}
];

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
                <Posts posts={posts} />
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

export {ArchiDiagramApp, EnvApp, PipelineApp, ProjectsApp, ProjectApp, SandboxApp, Index};

