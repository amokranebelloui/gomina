import React from "react";
import axios from "axios/index";
import {AppLayout, LoggedUserContext, PrimarySecondaryLayout} from "./common/layout";
import {ProjectBadge, ProjectMenu} from "../project/Project";
import "../project/Project.css"
import "../common/items.css"
import {CommitLog} from "../commitlog/CommitLog";
import {Container} from "../common/Container";
import {Documentation} from "../documentation/Documentation";
import queryString from 'query-string'
import {Dependencies} from "../dependency/Dependencies";
import {CallChain} from "../dependency/CallChain";
import Link from "react-router-dom/es/Link";

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        const queryParams = queryString.parse(this.props.location.search);
        this.state = {
            //projects: [],
            projectId: this.props.match.params.id,
            project: {},
            associated: [],

            dependencies: [],
            impacted: [],
            invocationChain: null,
            callChain: null,
            chainSelectedInvocation: [],
            chainSelectedCall: [],

            branchId: queryParams.branchId,
            branch: null,
            docId: this.props.match.params.docId,
            doc: null
            };
        //this.retrieveProjects = this.retrieveProjects.bind(this);
        this.retrieveProject = this.retrieveProject.bind(this);
        this.retrieveAssociated= this.retrieveAssociated.bind(this);
        //this.retrieveInstances= this.retrieveInstances.bind(this);
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
    retrieveAssociated(projectId) {
        const thisComponent = this;
        axios.get('/data/projects/' + projectId + '/associated')
            .then(response => {
                console.log("project", response.data);
                thisComponent.setState({associated: response.data});
            })
            .catch(function (error) {
                console.log("error", error.response);
                thisComponent.setState({associated: []});
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
        axios.get('/data/projects/' + projectId + '/scm?branchId=' + (branchId ? branchId : ""))
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
    selectChainDependency() {
        this.setState({chainSelectedInvocation: []});
        this.setState({chainSelectedCall: []});
    }
    selectInvocationDependency(child, parent) {
        const dep = {from: parent.serviceId, to: child.serviceId, functions: child.functions};
        this.setState({chainSelectedInvocation: [dep]});
    }
    selectCallDependency(child, parent) {
        const dep = {from: child.serviceId, to: parent.serviceId, functions: child.functions};
        this.setState({chainSelectedCall: [dep]});
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
    /*
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
*/
    componentDidMount() {
        console.info("projectApp !mount ", this.props.match.params.id);
        //this.retrieveProjects();
        if (this.state.projectId) {
            this.retrieveProject(this.state.projectId);
            this.retrieveAssociated(this.state.projectId);
            //this.retrieveInstances(this.state.projectId);
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
            else {
                this.retrieveBranch(this.state.projectId, null);
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
            this.retrieveAssociated(newProject);
            //this.retrieveInstances(newProject);
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
        console.info("change ", queryParams.branchId, newBranch, this.props.match.params.id, newProject);
        if (newProject &&
            (this.props.match.params.id !== newProject || queryParams.branchId !== newBranch)) {
            this.retrieveBranch(newProject, newBranch);
        }
    }
    render() {
        const queryParams = queryString.parse(this.props.location.search);
        const project = this.state.project;
        //const commits = project.commitLog || [];
        //const instances = this.state.instances.filter(instance => instance.project == project.id);
        //const title = (<span>Projects &nbsp;&nbsp;&nbsp;</span>);
        const docId = this.props.match.params.docId;
        const branchId = queryParams.branchId;
        //console.info("---1", this.state.associated);
        //console.info("---2", this.state.impacted);
        /*
        <hr />
                        <b>Dependencies</b>
                        <Dependencies dependencies={this.state.dependencies} />
                        <hr/>
                        <b>Impacted</b>
                        <Dependencies dependencies={this.state.impacted} />
                        
         */
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

                        <ProjectMenu project={project} />

                        {   branchId ? <b>Branch</b> :
                            docId ? <b>Doc</b> :
                            <b>Main</b>
                        }

                        {   docId
                            ? <Documentation doc={this.state.doc} />
                            : <CommitLog type={project.scmType} commits={(this.state.branch||{}).log} unresolved={(this.state.branch||{}).unresolved} />
                        }

                    </Container>
                    <div>
                        <ProjectBadge project={project}
                                      onReload={id => this.retrieveProject(id)}
                                      onReloadScm={id => this.reloadProject(id)}
                                      onReloadSonar={() => this.reloadSonar()} />
                        <hr/>
                        <h3>Other Projects</h3>
                        <div className="items">
                            {this.state.associated.map(projectRef =>
                                <Link to={"/component/" + projectRef.id}>{projectRef.label}</Link>
                            )}
                        </div>
                        <hr/>
                    </div>
                    <Container>
                        <h3>Invocation Chain</h3>
                        <CallChain chain={this.state.invocationChain} displayFirst={true}
                                   onDependencySelected={(child, parent) => this.selectInvocationDependency(child, parent)}/>
                        <Dependencies dependencies={this.state.chainSelectedInvocation} />
                        <hr />
                        <h3>Call Chain</h3>
                        <CallChain chain={this.state.callChain} displayFirst={true}
                                   onDependencySelected={(child, parent) => this.selectCallDependency(child, parent)} />
                        <Dependencies dependencies={this.state.chainSelectedCall} />

                    </Container>
                </PrimarySecondaryLayout>
            )}
            </LoggedUserContext.Consumer>
            </AppLayout>
        );
    }
}

export { ProjectApp };