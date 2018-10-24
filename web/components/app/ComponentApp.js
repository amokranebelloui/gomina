import React from "react";
import axios from "axios/index";
import {AppLayout, LoggedUserContext, PrimarySecondaryLayout} from "./common/layout";
import {ComponentBadge, ComponentMenu} from "../component/Component";
import "../component/Component.css"
import "../common/items.css"
import {CommitLog} from "../commitlog/CommitLog";
import {Container} from "../common/Container";
import {Documentation} from "../documentation/Documentation";
import queryString from 'query-string'
import {Dependencies} from "../dependency/Dependencies";
import {CallChain} from "../dependency/CallChain";
import Link from "react-router-dom/es/Link";

class ComponentApp extends React.Component {
    
    constructor(props) {
        super(props);
        const queryParams = queryString.parse(this.props.location.search);
        this.state = {
            componentId: this.props.match.params.id,
            component: {},
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
        this.retrieveComponent = this.retrieveComponent.bind(this);
        this.retrieveAssociated= this.retrieveAssociated.bind(this);
        this.retrieveBranch = this.retrieveBranch.bind(this);
        this.retrieveDoc = this.retrieveDoc.bind(this);
        this.reloadComponent = this.reloadComponent.bind(this);
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
    reloadComponent(componentId) {
        console.info("reloading", componentId);
        axios.post('/data/components/' + componentId + '/reload-scm')
            .then(response => {
                console.log("component reloaded", response.data);
            })
            .catch(function (error) {
                console.log("component reload error", error.response);
            });
    }
    reloadSonar(componentId) {
        axios.post('/data/components/reload-sonar')
            .then(response => {
                console.log("sonar reloaded", response.data);
            })
            .catch(function (error) {
                console.log("sonar reload error", error.response);
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
    componentDidMount() {
        console.info("componentApp !mount ", this.props.match.params.id);
        if (this.state.componentId) {
            this.retrieveComponent(this.state.componentId);
            this.retrieveAssociated(this.state.componentId);
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
        }
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
            //this.retrieveInstances(newComponent);
            this.retrieveDependencies(newComponent);
            this.retrieveImpacted(newComponent);
            this.retrieveInvocationChain(newComponent);
            this.retrieveCallChain(newComponent);
            this.selectChainDependency()
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
            <LoggedUserContext.Consumer>
            {loggedUser => (
                <PrimarySecondaryLayout>
                    <Container>
                        <ComponentBadge component={component}
                                        onReload={id => this.retrieveComponent(id)}
                                        onReloadScm={id => this.reloadComponent(id)}
                                        onReloadSonar={() => this.reloadSonar()} />

                        <ComponentMenu component={component} />

                        {   branchId ? <b>Branch</b> :
                            docId ? <b>Doc</b> :
                            <b>Main</b>
                        }

                        {   docId
                            ? <Documentation doc={this.state.doc} />
                            : <CommitLog type={component.scmType} commits={(this.state.branch||{}).log} unresolved={(this.state.branch||{}).unresolved} />
                        }

                    </Container>
                    <div>
                        <ComponentBadge component={component}
                                        onReload={id => this.retrieveComponent(id)}
                                        onReloadScm={id => this.reloadComponent(id)}
                                        onReloadSonar={() => this.reloadSonar()} />
                        <hr/>
                        <h3>Other Components</h3>
                        <div className="items">
                            {this.state.associated.map(componentRef =>
                                <Link to={"/component/" + componentRef.id}>{componentRef.label}</Link>
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

export { ComponentApp };