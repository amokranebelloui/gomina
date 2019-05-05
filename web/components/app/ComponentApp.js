import React, {Fragment} from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import {ComponentBadge, ComponentMenu} from "../component/Component";
import "../component/Component.css"
import "../common/items.css"
import {CommitLog, CommitLogLegend} from "../commitlog/CommitLog";
import {Container} from "../common/Container";
import {Documentation} from "../documentation/Documentation";
import queryString from 'query-string'
import {Dependencies} from "../dependency/Dependencies";
import {CallChain} from "../dependency/CallChain";
import Link from "react-router-dom/es/Link";
import {ScmLink} from "../component/ScmLink";
import {UnreleasedChangeCount} from "../component/UnreleasedChangeCount";
import {ApiDefinition} from "../component/ApiDefinition";
import {ApiUsage} from "../component/ApiUsage";
import {ApiDefinitionEditor} from "../component/ApiDefinitionEditor";
import {LoggedUserContext, Secure} from "../permission/Secure";
import {Events} from "../environment/Events";
import {Knowledge} from "../knowledge/Knowledge";
import {DateTime} from "../common/DateTime";

class ComponentApp extends React.Component {
    
    constructor(props) {
        super(props);
        const queryParams = queryString.parse(this.props.location.search);
        this.state = {
            componentId: this.props.match.params.id,
            component: {},
            associated: [],

            versions: [],
            libraries: [],

            api: [],
            usage: [],
            dependencies: [],
            impacted: [],
            invocationChain: null,
            callChain: null,
            chainSelectedInvocation: [],
            chainSelectedCall: [],

            branchId: queryParams.branchId,
            branch: null,
            docId: this.props.match.params.docId,
            doc: null,

            events: []
            };
        this.retrieveComponent = this.retrieveComponent.bind(this);
        this.retrieveAssociated= this.retrieveAssociated.bind(this);
        this.retrieveBranch = this.retrieveBranch.bind(this);
        this.retrieveDoc = this.retrieveDoc.bind(this);
        this.reloadScm = this.reloadScm.bind(this);
        console.info("componentApp !constructor ", this.props.match.params);
    }
    retrieveComponent(componentId) {
        const thisComponent = this;
        axios.get('/data/components/' + componentId)
            .then(response => {
                console.log("components", response.data);
                thisComponent.setState({component: response.data});
            })
            .catch(function (error) {
                console.log("error", error.response);
                thisComponent.setState({component: {}});
            });
    }
    retrieveAssociated(componentId) {
        const thisComponent = this;
        axios.get('/data/components/' + componentId + '/associated')
            .then(response => {
                console.log("components", response.data);
                thisComponent.setState({associated: response.data});
            })
            .catch(function (error) {
                console.log("error", error.response);
                thisComponent.setState({associated: []});
            });
    }
    reloadScm(componentId) {
        console.info("reloading", componentId);
        axios.put('/data/components/reload-scm?componentIds=' + componentId)
            .then(response => {
                console.log("component reloaded", response.data);
                this.retrieveComponent(componentId)
                this.retrieveBranch(this.state.componentId, this.state.branchId);
            })
            .catch(function (error) {
                console.log("component reload error", error.response);
            });
    }
    reloadBuild(componentId) {
        console.info("reloading build ", componentId);
        axios.put('/data/components/reload-build?componentIds=' + componentId)
            .then(response => {
                console.log("component build reloaded", response.data);
                this.retrieveComponent(componentId)
            })
            .catch(function (error) {
                console.log("component build reload error", error.response);
            });
    }
    reloadSonar(componentId) {
        axios.put('/data/components/reload-sonar?componentIds=' + componentId)
            .then(response => {
                console.log("sonar reloaded", response.data);
                this.retrieveComponent(componentId)
            })
            .catch(function (error) {
                console.log("sonar reload error", error.response);
            });
    }
    reloadEvents() {
        const thisComponent = this;
        axios.post('/data/events/reload-events')
            .then(() => this.retrieveEvents(thisComponent.state.env))
            .catch((error) => console.log("reload error", error.response))
    }
    editLabel(componentId, label) {
        axios.put('/data/components/' + componentId + '/label?label=' + label)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit label", error.response));
    }
    editType(componentId, type) {
        axios.put('/data/components/' + componentId + '/type?type=' + type)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit type", error.response));
    }
    editArtifactId(componentId, artifactId) {
        axios.put('/data/components/' + componentId + '/artifactId?id=' + artifactId)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit artifactId", error.response));
    }
    editScm(componentId, type, url, path, hasMetadata) {
        axios.put('/data/components/' + componentId + '/scm?type=' + type + '&url=' + url + '&path=' + path + '&hasMetadata=' + hasMetadata)
            .then(() => {
                this.retrieveComponent(componentId);
                this.retrieveBranch(componentId, this.state.branchId);
            })
            .catch((error) => console.error("cannot edit scm", error.response));
    }
    editInceptionDate(componentId, date) {
        axios.put('/data/components/' + componentId + '/inceptionDate?inceptionDate=' + date)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit inception date", error.response));
    }
    editOwner(componentId, owner) {
        axios.put('/data/components/' + componentId + '/owner?owner=' + owner)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit owner", error.response));
    }
    editCriticity(componentId, date) {
        axios.put('/data/components/' + componentId + '/criticity?criticity=' + date)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit criticity", error.response));
    }

    editSonar(componentId, server) {
        axios.put('/data/components/' + componentId + '/sonar?server=' + server)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit sonar", error.response));
    }
    editBuild(componentId, server, job) {
        axios.put('/data/components/' + componentId + '/build?server=' + server + '&job=' + job)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot edit buil", error.response));
    }
    addSystem(componentId, system) {
        axios.put('/data/components/' + componentId + '/add-system/' + system)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot add system", error.response));
    }
    deleteSystem(componentId, system) {
        axios.put('/data/components/' + componentId + '/delete-system/' + system)
            .then(() => this.retrieveComponent(componentId))
            .catch(error => console.error("cannot delete system", error.response));
    }
    addLanguage(componentId, language) {
        console.info("add language", componentId, language);
        axios.put('/data/components/' + componentId + '/add-language/' + language)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot add language", error.response));
    }
    deleteLanguage(componentId, language) {
        axios.put('/data/components/' + componentId + '/delete-language/' + language)
            .then(() => this.retrieveComponent(componentId))
            .catch(error => console.error("cannot delete language", error.response));
    }
    addTag(componentId, tag) {
        axios.put('/data/components/' + componentId + '/add-tag/' + tag)
            .then(() => this.retrieveComponent(componentId))
            .catch((error) => console.error("cannot add tag", error.response));
    }
    deleteTag(componentId, tag) {
        axios.put('/data/components/' + componentId + '/delete-tag/' + tag)
            .then(() => this.retrieveComponent(componentId))
            .catch(error => console.error("cannot delete tag", error.response));
    }
    changeKnowledge(componentId, userId, knowledge) {
        axios.put('/data/knowledge/' + componentId + '/user/' + userId + '?knowledge=' + knowledge)
            .then(() => this.retrieveKnowledge(componentId))
            .catch(error => console.error("cannot change knowledge for " + componentId + " " + userId, error.response));
    }
    enable(componentId) {
        console.info("enable ", componentId);
        axios.put('/data/components/' + componentId + '/enable')
            .then(response => {
                console.log("component enabled", response.data);
                this.retrieveComponent(componentId)
            })
            .catch(function (error) {
                console.log("component enable error", error.response);
            });
    }
    disable(componentId) {
        console.info("disable ", componentId);
        axios.put('/data/components/' + componentId + '/disable')
            .then(response => {
                console.log("component disabled", response.data);
                this.retrieveComponent(componentId)
            })
            .catch(function (error) {
                console.log("component disable error", error.response);
            });
    }
    delete(componentId) {
        console.info("delete ", componentId);
        axios.delete('/data/components/' + componentId + '/delete')
            .then(response => {
                console.log("component delete", response.data);
                this.retrieveComponent(componentId)
            })
            .catch(function (error) {
                console.log("component delete error", error.response);
            });
    }
    retrieveBranch(componentId, branchId) {
        const thisComponent = this;
        axios.get('/data/components/' + componentId + '/scm?branchId=' + (branchId ? branchId : ""))
            .then(response => {
                console.log("branch data", response.data);
                thisComponent.setState({branch: response.data});
            })
            .catch(function (error) {
                console.log("branch error", error.response);
                thisComponent.setState({branch: null});
            });
    }
    retrieveVersions(componentId) {
        const thisComponent = this;
        axios.get('/data/components/' + componentId + '/versions')
            .then(response => thisComponent.setState({versions: response.data}))
            .catch(() => thisComponent.setState({versions: null}))
    }
    retrieveLibraries(componentId) {
        const thisComponent = this;
        axios.get('/data/dependencies/libraries/' + componentId)
            .then(response => thisComponent.setState({libraries: response.data}))
            .catch(() => thisComponent.setState({libraries: null}))
    }
    retrieveApi(componentId) {
        const thisComponent = this;
        axios.get('/data/dependencies/api/' + componentId)
            .then(response => {
                console.log("API data");
                thisComponent.setState({api: response.data});
            })
            .catch(function (error) {
                console.log("API error", error.response);
                thisComponent.setState({api: null});
            });
    }
    addApi(componentId, fName, fType) {
        axios.post('/data/dependencies/api/' + componentId + '/add', {name: fName, type: fType})
            .then(() => this.retrieveApi(componentId))
            .catch((error) => console.log("component api add error", error.response));
    }
    removeApi(componentId, fName, fType) {
        axios.delete('/data/dependencies/api/' + componentId + '/remove', { data: {name: fName, type: fType} })
            .then(() => this.retrieveApi(componentId))
            .catch((error) => console.log("component api delete error", error.response));
    }
    retrieveUsage(componentId) {
        const thisComponent = this;
        axios.get('/data/dependencies/usage/' + componentId)
            .then(response => {
                console.log("Usage data");
                thisComponent.setState({usage: response.data});
            })
            .catch(function (error) {
                console.log("Usage error", error.response);
                thisComponent.setState({usage: null});
            });
    }
    addUsage(componentId, fName, fType, fUsage) {
        axios.post('/data/dependencies/usage/' + componentId + '/add', {name: fName, type: fType, usage: fUsage})
            .then(() => this.retrieveUsage(componentId))
            .catch((error) => console.log("component usage delete error", error.response));
    }
    removeUsage(componentId, fName, fType) {
        axios.delete('/data/dependencies/usage/' + componentId + '/remove', { data: {name: fName, type: fType} })
            .then(() => this.retrieveUsage(componentId))
            .catch((error) => console.log("component usage delete error", error.response));
    }
    retrieveDependencies(componentId) {
        const thisComponent = this;
        axios.get('/data/dependencies/outgoing/' + componentId)
            .then(response => {
                console.log("deps data");
                thisComponent.setState({dependencies: response.data});
            })
            .catch(function (error) {
                console.log("deps error", error.response);
                thisComponent.setState({dependencies: null});
            });
    }
    retrieveImpacted(componentId) {
        const thisComponent = this;
        axios.get('/data/dependencies/incoming/' + componentId)
            .then(response => {
                console.log("impacted data");
                thisComponent.setState({impacted: response.data});
            })
            .catch(function (error) {
                console.log("impacted error", error.response);
                thisComponent.setState({impacted: null});
            });
    }
    retrieveInvocationChain(componentId) {
        const thisComponent = this;
        axios.get('/data/dependencies/invocation/chain/' + componentId)
            .then(response => {
                console.log("invocation chain data", response.data);
                thisComponent.setState({invocationChain: response.data});
            })
            .catch(function (error) {
                console.log("invocation chain error", error.response);
                thisComponent.setState({invocationChain: null});
            });
    }
    retrieveCallChain(componentId) {
        const thisComponent = this;
        axios.get('/data/dependencies/call/chain/' + componentId)
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
    retrieveDoc(componentId, docId) {
        const thisComponent = this;
        axios.get('/data/components/' + componentId + '/doc/' + docId)
            .then(response => {
                console.log("doc data");
                thisComponent.setState({doc: response.data});
            })
            .catch(function (error) {
                console.log("doc error", error.response);
                thisComponent.setState({doc: null});
            });
    }
    retrieveEvents() {
        const thisComponent = this;
        axios.get('/data/events/component/' + this.state.componentId)
            .then(response => {
                console.info("events", typeof response.data, response.data.events)
                thisComponent.setState({events: response.data.events})
            })
            .catch(() => thisComponent.setState({events: []}))
    }
    retrieveKnowledge() {
        const thisComponent = this;
        axios.get('/data/knowledge/component/' + this.state.componentId)
            .then(response => thisComponent.setState({knowledge: response.data}))
            .catch(() => thisComponent.setState({knowledge: []}))
    }
    componentKnowledge(userId: string) {
        const knowledge = this.state.knowledge && this.state.knowledge.find(k => k.user.id === userId);
        return knowledge && knowledge.knowledge
    }
    componentDidMount() {
        console.info("componentApp !mount ", this.props.match.params.id);

        const thisComponent = this;
        axios.get('/data/components/build/servers')
            .then(response => thisComponent.setState({buildServers: response.data}))
            .catch(() => thisComponent.setState({buildServers: []}));
        axios.get('/data/components/sonar/servers')
            .then(response => thisComponent.setState({sonarServers: response.data}))
            .catch(() => thisComponent.setState({sonarServers: []}));

        if (this.state.componentId) {
            this.reloadAll();
        }
    }

    reloadAll() {
        this.retrieveComponent(this.state.componentId);
        this.retrieveAssociated(this.state.componentId);
        this.retrieveVersions(this.state.componentId);
        this.retrieveLibraries(this.state.componentId);
        this.retrieveApi(this.state.componentId);
        this.retrieveUsage(this.state.componentId);
        this.retrieveDependencies(this.state.componentId);
        this.retrieveImpacted(this.state.componentId);
        this.retrieveInvocationChain(this.state.componentId);
        this.retrieveCallChain(this.state.componentId);
        this.selectChainDependency();
        if (this.state.docId) {
            this.retrieveDoc(this.state.componentId, this.state.docId);
        }
        if (this.state.branchId) {
            this.retrieveBranch(this.state.componentId, this.state.branchId);
        }
        else {
            this.retrieveBranch(this.state.componentId, null);
        }
        this.retrieveEvents();
        this.retrieveKnowledge()
    }

    componentWillReceiveProps(nextProps) {
        const queryParams = queryString.parse(this.props.location.search);
        const nextQueryParams = queryString.parse(nextProps.location.search);
        const newComponent = nextProps.match.params.id;
        const newDoc = nextProps.match.params.docId;
        const newBranch = nextQueryParams.branchId;
        console.info("componentApp !props-chg ", this.props.match.params.id, newComponent);
        console.info("branch", queryParams.branchId, newBranch);
        this.setState({componentId: newComponent});
        if (this.props.match.params.id !== newComponent && newComponent) {
            this.retrieveComponent(newComponent);
            this.retrieveAssociated(newComponent);
            this.retrieveVersions(newComponent);
            this.retrieveLibraries(newComponent);
            //this.retrieveInstances(newComponent);
            this.retrieveApi(newComponent);
            this.retrieveUsage(newComponent);
            this.retrieveDependencies(newComponent);
            this.retrieveImpacted(newComponent);
            this.retrieveInvocationChain(newComponent);
            this.retrieveCallChain(newComponent);
            this.selectChainDependency();
            this.retrieveEvents();
            this.retrieveKnowledge()
        }
        if (newComponent && newDoc &&
            (this.props.match.params.id !== newComponent || this.props.match.params.docId !== newDoc)) {
            this.retrieveDoc(newComponent, newDoc);
        }
        console.info("change ", queryParams.branchId, newBranch, this.props.match.params.id, newComponent);
        if (newComponent &&
            (this.props.match.params.id !== newComponent || queryParams.branchId !== newBranch)) {
            this.retrieveBranch(newComponent, newBranch);
        }
    }
    render() {
        const queryParams = queryString.parse(this.props.location.search);
        const component = this.state.component;
        const docId = this.props.match.params.docId;
        const branchId = queryParams.branchId;
        /*
        <Dependencies dependencies={this.state.dependencies} />
        <Dependencies dependencies={this.state.impacted} />
         */
        return (
            <AppLayout title={"Component: " + component.label}>
                <PrimarySecondaryLayout>
                    <Container>
                        <span title={component.id}>
                            <span style={{fontSize: 14}}><b>{component.label}</b></span>
                            {component.disabled &&
                                <span>&nbsp;<s>DISABLED</s></span>
                            }
                            <span style={{fontSize: 8, marginLeft: 2}}>({component.type})</span>
                        </span>
                        <br/>
                        <span style={{fontSize: 9}}>{component.artifactId}</span>
                        <br/>

                        <ScmLink type={component.scmType} />&nbsp;
                        <span style={{fontSize: 9}}>{component.scmLocation ? component.scmLocation : 'not under scm'}</span>
                        <br/>
                        <UnreleasedChangeCount changes={component.changes} />

                        <ComponentMenu component={component} />

                        {   branchId ? <b>Branch</b> :
                            docId ? <b>Doc</b> :
                            <b>Main</b>
                        }

                        {   docId
                            ? <Documentation doc={this.state.doc} />
                            : <Fragment>
                                <CommitLog type={component.scmType} commits={(this.state.branch||{}).log} unresolved={(this.state.branch||{}).unresolved} />
                                <CommitLogLegend />
                              </Fragment>
                        }

                    </Container>
                    <div>
                        <LoggedUserContext.Consumer>
                            {loggedUser =>
                                <ComponentBadge buildServers={this.state.buildServers}
                                                sonarServers={this.state.sonarServers}
                                                component={component}
                                                knowledge={this.componentKnowledge(loggedUser.userId)}
                                                onReload={id => this.reloadAll(id)}
                                                onReloadScm={id => this.reloadScm(id)}
                                                onReloadBuild={id => this.reloadBuild(id)}
                                                onReloadSonar={id => this.reloadSonar(id)}
                                                onLabelEdited={(id, l) => this.editLabel(id, l)}
                                                onTypeEdited={(id, t) => this.editType(id, t)}
                                                onArtifactIdEdited={(id, artifactId) => this.editArtifactId(id, artifactId)}
                                                onScmEdited={(id, t, u, p, md) => this.editScm(id, t, u, p, md)}
                                                onInceptionDateEdited={(id, d) => this.editInceptionDate(id, d)}
                                                onOwnerEdited={(id, o) => this.editOwner(id, o)}
                                                onCriticityEdited={(id, c) => this.editCriticity(id, c)}
                                                onSonarEdited={(id, s) => this.editSonar(id, s)}
                                                onBuildEdited={(id, s, j) => this.editBuild(id, s, j)}
                                                onSystemAdd={(id, s) => this.addSystem(id, s)}
                                                onSystemDelete={(id, s) => this.deleteSystem(id, s)}
                                                onLanguageAdd={(id, l) => this.addLanguage(id, l)}
                                                onLanguageDelete={(id, l) => this.deleteLanguage(id, l)}
                                                onTagAdd={(id, t) => this.addTag(id, t)}
                                                onTagDelete={(id, t) => this.deleteTag(id, t)}
                                                onKnowledgeChange={(id, k) => this.changeKnowledge(id, loggedUser.userId, k)}
                                                onEnable={id => this.enable(id)}
                                                onDisable={id => this.disable(id)}
                                                onDelete={id => this.delete(id)}
                                />
                            }
                        </LoggedUserContext.Consumer>
                        <br/>
                    </div>
                    <Container>
                        <h3>Other Components</h3>
                        <div className="items">
                            {this.state.associated.map(componentRef =>
                                <Link to={"/component/" + componentRef.id}>{componentRef.label || componentRef.id}</Link>
                            )}
                        </div>
                        <br/>
                        <h3>Versions</h3>
                        <div className="items">
                            {this.state.versions.map(versionRelease =>
                                <span>
                                    <span>{versionRelease.artifactId}</span>&nbsp;
                                    <span>{versionRelease.version.version}</span>&nbsp;
                                    <DateTime date={versionRelease.releaseDate} />
                                    <br/>
                                </span>
                            )}
                        </div>
                        <br/>
                        <h3>Libraries</h3>
                        <div className="items">
                            {this.state.libraries.map(library =>
                                <Fragment>
                                    <span>{library}</span>
                                    <br/>
                                </Fragment>
                            )}
                        </div>
                        <br/>
                        <h3>Knowledge</h3>
                        <Knowledge knowledge={this.state.knowledge} hideComponent={true} />
                        <br/>
                        <h3>API</h3>
                        {(this.state.api || []).map(api =>
                            <ApiDefinition api={api} onManualApiRemove={() => this.removeApi(component.id, api.name, api.type)} />
                        )}
                        <Secure permission="component.edit.api">
                            <ApiDefinitionEditor type="definition" api={{name: "", type: ""}} onAdd={(f) => this.addApi(component.id, f.name, f.type)} />
                        </Secure>
                        <br/>
                        
                        <h3>Usage</h3>
                        {(this.state.usage || []).map(usage =>
                            <ApiUsage usage={usage} onManualUsageRemove={() => this.removeUsage(component.id, usage.name, usage.type)} />
                        )}
                        <Secure permission="component.edit.usage">
                            <ApiDefinitionEditor type="usage" api={{name: "", type: ""}} onAdd={(f) => this.addUsage(component.id, f.name, f.type, f.usage)} />
                        </Secure>
                        <br/>
                        
                        <h3>Invocation Chain</h3>
                        <CallChain chain={this.state.invocationChain} displayFirst={true}
                                   onDependencySelected={(child, parent) => this.selectInvocationDependency(child, parent)}/>
                        <Dependencies dependencies={this.state.chainSelectedInvocation} />
                        <br/>
                        
                        <h3>Call Chain</h3>
                        <CallChain chain={this.state.callChain} displayFirst={true}
                                   onDependencySelected={(child, parent) => this.selectCallDependency(child, parent)} />
                        <Dependencies dependencies={this.state.chainSelectedCall} />

                        <h3>Events</h3>
                        <button onClick={e => this.reloadEvents()}>Reload Events</button>
                        <Container>
                            <Events events={this.state.events || []} errors={[]} />
                        </Container>

                    </Container>
                </PrimarySecondaryLayout>
            )}
            </AppLayout>
        );
    }
}

export { ComponentApp };