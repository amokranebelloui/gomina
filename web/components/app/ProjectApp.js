import React from "react";
import axios from "axios/index";
import {AppLayout, LoggedUserContext, PrimarySecondaryLayout} from "./common/layout";
import {ProjectBadge} from "../project/Project";
import "../project/Project.css"
import {CommitLog} from "../commitlog/CommitLog";
import {Container} from "../common/Container";
import {Documentation} from "../documentation/Documentation";
import queryString from 'query-string'
import {Dependencies} from "../dependency/Dependencies";
import {CallChain} from "../dependency/CallChain";

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        const queryParams = queryString.parse(this.props.location.search);
        this.state = {
            //projects: [],
            projectId: this.props.match.params.id,
            project: {},
            instances: [],

            dependencies: [],
            impacted: [],
            invocationChain: null,
            callChain: null,
            chainSelectedDependencies: [],

            branchId: queryParams.branchId,
            branch: null,
            docId: this.props.match.params.docId,
            doc: null
            };
        //this.retrieveProjects = this.retrieveProjects.bind(this);
        this.retrieveProject = this.retrieveProject.bind(this);
        this.retrieveInstances= this.retrieveInstances.bind(this);
        this.retrieveBranch = this.retrieveBranch.bind(this);
        this.retrieveDoc = this.retrieveDoc.bind(this);
        this.reloadProject = this.reloadProject.bind(this);
        console.info("projectApp !constructor ", this.props.match.params);
    }
    /*
    retrieveProjects() {
        console.log("projectApp Retr Projects ... ");
        const thisComponent = this;
        axios.get('/data/projects')
            .then(response => {
                console.log("projectApp data projects", response.data);
                thisComponent.setState({projects: response.data});
            })
            .catch(function (error) {
                console.log("projectApp error", error);
                thisComponent.setState({projects: []});
            });
    }
    */
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
        console.info("reloading", projectId);
        axios.post('/data/projects/' + projectId + '/reload-scm')
            .then(response => {
                console.log("project reloaded", response.data);
            })
            .catch(function (error) {
                console.log("project reload error", error.response);
            });
    }
    reloadSonar(projectId) {
        axios.post('/data/projects/reload-sonar')
            .then(response => {
                console.log("sonar reloaded", response.data);
            })
            .catch(function (error) {
                console.log("sonar reload error", error.response);
            });
    }
    retrieveBranch(projectId, branchId) {
        const thisComponent = this;
        axios.get('/data/projects/' + projectId + '/scm?branchId=' + branchId)
            .then(response => {
                console.log("branch data", response.data);
                thisComponent.setState({branch: response.data});
            })
            .catch(function (error) {
                console.log("branch error", error.response);
                thisComponent.setState({branch: null});
            });
    }
    retrieveDependencies(projectId) {
        const thisComponent = this;
        axios.get('/data/dependencies/outgoing/' + projectId)
            .then(response => {
                console.log("deps data");
                thisComponent.setState({dependencies: response.data});
            })
            .catch(function (error) {
                console.log("deps error", error.response);
                thisComponent.setState({dependencies: null});
            });
    }
    retrieveImpacted(projectId) {
        const thisComponent = this;
        axios.get('/data/dependencies/incoming/' + projectId)
            .then(response => {
                console.log("impacted data");
                thisComponent.setState({impacted: response.data});
            })
            .catch(function (error) {
                console.log("impacted error", error.response);
                thisComponent.setState({impacted: null});
            });
    }
    retrieveInvocationChain(projectId) {
        const thisComponent = this;
        axios.get('/data/dependencies/invocation/chain/' + projectId)
            .then(response => {
                console.log("invocation chain data", response.data);
                thisComponent.setState({invocationChain: response.data});
            })
            .catch(function (error) {
                console.log("invocation chain error", error.response);
                thisComponent.setState({invocationChain: null});
            });
    }
    retrieveCallChain(projectId) {
        const thisComponent = this;
        axios.get('/data/dependencies/call/chain/' + projectId)
            .then(response => {
                console.log("call chain data", response.data);
                thisComponent.setState({callChain: response.data});
            })
            .catch(function (error) {
                console.log("call chain error", error.response);
                thisComponent.setState({callChain: null});
            });
    }
    selectChainDependency(child, parent, invert) {
        if (child && parent) {
            const dep = invert
                ? {from: child.projectId, to: parent.projectId, functions: child.functions}
                : {from: parent.projectId, to: child.projectId, functions: child.functions}
                ;
            //console.info("Call Selected", child, parent, dep);
            this.setState({chainSelectedDependencies: [dep]});
        }
        else {
            //console.info("Call NONE Selected", child, parent);
            this.setState({chainSelectedDependencies: []});
        }
    }
    retrieveDoc(projectId, docId) {
        const thisComponent = this;
        axios.get('/data/projects/' + projectId + '/doc/' + docId)
            .then(response => {
                console.log("doc data");
                thisComponent.setState({doc: response.data});
            })
            .catch(function (error) {
                console.log("doc error", error.response);
                thisComponent.setState({doc: null});
            });
    }
    retrieveInstances(projectId) {
        console.log("projectApp Retr Instances... " + projectId);
        const thisComponent = this;
        axios.get('/data/instances') // FIXME Retrieve only instances corresponding to project
            .then(response => {
                console.log("projectApp data instances", response.data);
                thisComponent.setState({instances: response.data});
            })
            .catch(function (error) {
                console.log("projectApp error", error);
                thisComponent.setState({instances: []});
            });
    }

    componentDidMount() {
        console.info("projectApp !mount ", this.props.match.params.id);
        //this.retrieveProjects();
        if (this.state.projectId) {
            this.retrieveProject(this.state.projectId);
            this.retrieveInstances(this.state.projectId);
            this.retrieveDependencies(this.state.projectId);
            this.retrieveImpacted(this.state.projectId);
            this.retrieveInvocationChain(this.state.projectId);
            this.retrieveCallChain(this.state.projectId);
            this.selectChainDependency();
            if (this.state.docId) {
                this.retrieveDoc(this.state.projectId, this.state.docId);
            }
            if (this.state.branchId) {
                this.retrieveBranch(this.state.projectId, this.state.branchId);
            }
        }
    }
    componentWillReceiveProps(nextProps) {
        const queryParams = queryString.parse(this.props.location.search);
        const nextQueryParams = queryString.parse(nextProps.location.search);
        const newProject = nextProps.match.params.id;
        const newDoc = nextProps.match.params.docId;
        const newBranch = nextQueryParams.branchId;
        console.info("projectApp !props-chg ", this.props.match.params.id, newProject);
        console.info("project branch", queryParams.branchId, newBranch);
        this.setState({projectId: newProject});
        if (this.props.match.params.id !== newProject && newProject) {
            this.retrieveProject(newProject);
            this.retrieveInstances(newProject);
            this.retrieveDependencies(newProject);
            this.retrieveImpacted(newProject);
            this.retrieveInvocationChain(newProject);
            this.retrieveCallChain(newProject);
            this.selectChainDependency()
        }
        if (newProject && newDoc &&
            (this.props.match.params.id !== newProject || this.props.match.params.docId !== newDoc)) {
            this.retrieveDoc(newProject, newDoc);
        }
        if (newProject && newBranch &&
            (this.props.match.params.id !== newProject || queryParams.branchId !== newBranch)) {
            this.retrieveBranch(newProject, newBranch);
        }
    }
    render() {
        const queryParams = queryString.parse(this.props.location.search);
        const project = this.state.project;
        const commits = project.commitLog || [];
        const instances = this.state.instances.filter(instance => instance.project == project.id);
        const title = (<span>Projects &nbsp;&nbsp;&nbsp;</span>);
        const docId = this.props.match.params.docId;
        const branchId = queryParams.branchId;
        console.info("---", docId, branchId);
        return (
            <AppLayout title={"Component: " + project.label}>
            <LoggedUserContext.Consumer>
            {loggedUser => (
                <PrimarySecondaryLayout>
                    <Container>
                        <ProjectBadge project={project}
                                      onReload={id => this.retrieveProject(id)}
                                      onReloadScm={id => this.reloadProject(id)}
                                      onReloadSonar={() => this.reloadSonar()} />
                        {   branchId ? [<b>Branch</b>,<CommitLog type={project.scmType} commits={this.state.branch}  />] :
                            docId ? [<b>Doc</b>,<Documentation doc={this.state.doc} />] :
                                this.props.location.pathname.endsWith("dependencies") ? [
                                        <b>Invocation Chain</b>,
                                        <CallChain chain={this.state.invocationChain} displayFirst={true}
                                                   onDependencySelected={(child, parent) => this.selectChainDependency(child, parent, false)}/>,
                                        <Dependencies dependencies={this.state.chainSelectedDependencies} />,
                                        <hr />,
                                        <b>Dependencies</b>,
                                        <Dependencies dependencies={this.state.dependencies} />
                                    ] :
                                    this.props.location.pathname.endsWith("impacted") ? [
                                            <b>Call Chain</b>,
                                            <CallChain chain={this.state.callChain} displayFirst={true}
                                                       onDependencySelected={(child, parent) => this.selectChainDependency(child, parent, true)} />,
                                            <Dependencies dependencies={this.state.chainSelectedDependencies} />,
                                            <hr />,
                                            <b>Impacted</b>,
                                            <Dependencies dependencies={this.state.impacted} />
                                        ] :
                                        [<b>Commit Log</b>,<CommitLog type={project.scmType} commits={commits} instances={instances} />]

                        }
                    </Container>
                    <div>
                        <ProjectBadge project={project}
                                      onReload={id => this.retrieveProject(id)}
                                      onReloadScm={id => this.reloadProject(id)}
                                      onReloadSonar={() => this.reloadSonar()} />
                    </div>
                    <Container>

                    </Container>
                </PrimarySecondaryLayout>
            )}
            </LoggedUserContext.Consumer>
            </AppLayout>
        );
    }
}

export { ProjectApp };