import React from "react";
import {groupBy, Toggle} from "./gomina";
import {AppLayout} from "./layout";
import {Diagram} from "./archi-diagram";
import {ProjectPipeline} from "./pipeline";
import {Coverage, LinesOfCode, ProjectSummary, ScmLog} from "./project";
import {LoggingButton, MyComponent, Posts, Toggle2, WarningBanner} from "./sandbox";
import C1 from "./module.js";
import axios from "axios";

class ArchiDiagramApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {components: [], dependencies: []};
        this.addData = this.addData.bind(this);
        this.removeData = this.removeData.bind(this);
        this.componentMoved = this.componentMoved.bind(this);
    }
    componentDidMount() {
        axios.get('/data/diagram/data')
            .then(response => {
                console.log("diagram data", response.data);
                this.setState({components: response.data.components, dependencies: response.data.dependencies});
                console.info("state ", this.state)
            })
            .catch(function (error) {
                console.log("diagram data error", error.response);
            });
    }
    addData() {
        //const a = Math.floor((Math.random() * 10));
        //const b = Math.floor((Math.random() * 10));
        //const newDeps = [].concat(this.state.dependencies, [{from: a, to: b}]);
        //this.setState({dependencies: dependencies});
        console.info("Reload: add");
    }
    removeData() {
        //this.state.dependencies.pop();
        //const newDeps = this.state.dependencies;
        //this.setState({dependencies: dependencies2});
        console.info("Reload: rem");
    }
    componentMoved(d) {
        console.info("moved 2", d);

        axios.post('/data/diagram/update', d)
            .then(response => {
                console.log("reloaded", response.data);
            })
            .catch(function (error) {
                console.log("reload error", error.response);
            });
    }
    render() {
        return (
            <AppLayout title="Architecture Diagram">
                <button onClick={this.addData}>Add</button>
                <button onClick={this.removeData}>Remove</button>
                <Diagram components={this.state.components}
                         dependencies={this.state.dependencies}
                         onLinkSelected={d => console.info("selected", d)}
                         onComponentMoved={this.componentMoved} />
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
        this.reloadProject = this.reloadProject.bind(this);
        console.info("projectApp !constructor ", this.props.match.params.id);
    }
    componentWillMount() {
        console.info("projectApp !mount ", this.props.match.params.id);
        //this.retrieveProject(this.props.match.params.id);
    }
    componentWillReceiveProps(nextProps) {
        console.info("projectApp !props-chg ", this.props.match.params.id, nextProps.match.params.id);
        this.retrieveProject(nextProps.match.params.id);
    }
    retrieveProject(projectId) {
        const thisComponent = this;
        axios.get('/data/projects/' + projectId)
            .then(response => {
                console.log("project", response.data);
                thisComponent.setState({project: response.data});
            })
            .catch(function (error) {
                console.log("error", error.response);
                thisComponent.setState({project: {}});
            });
    }
    reloadProject(projectId) {
        axios.post('/data/projects/' + projectId + '/reload')
            .then(response => {
                console.log("project reloaded", response.data);
            })
            .catch(function (error) {
                console.log("project reload error", error.response);
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
                        <div>
                            <span title={project.id}>
                                <b>{project.label}</b>
                                <span style={{fontSize: 10, marginLeft: 2}}>({project.type})</span>
                            </span>
                            <br/>
                            <span style={{fontSize: 11}}>{project.mvn}</span>
                            <br/>
                            <span style={{fontSize: 11}}>{project.repo + ':' + project.svn}</span>
                            <button onClick={e => this.reloadProject(project.id)}>RELOAD</button>
                            <br/>
                            <LinesOfCode loc={project.loc} />&nbsp;
                            <Coverage coverage={project.coverage} />&nbsp;
                        </div>
                        <br/>
                        <ScmLog project={project} commits={commits} instances={instances} />
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

export {ArchiDiagramApp, PipelineApp, ProjectsApp, ProjectApp, SandboxApp, Index};

