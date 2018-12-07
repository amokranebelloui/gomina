import React from "react";
import SockJS from "sockjs-client";
import axios from "axios/index";
import {flatMap, groupBy} from "../common/utils";
import {EnvironmentLogical} from "../environment/Environment";
import {AppLayout} from "./common/layout";
//import {Link} from "react-router-dom";
import {Toggle} from "../common/Toggle";
import {Container} from "../common/Container";
import {InstanceFilter} from "../environment/InstanceFilter";
import {Events} from "../environment/Events";
import {InstanceProperties} from "../environment/Instance";
import {Well} from "../common/Well";
import {extendSystems} from "../system/system-utils";
import {TagCloud} from "../common/TagCloud";
import Link from "react-router-dom/es/Link";

class EnvApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            env: props.match.params.id,
            envs: [],
            realtime: false,
            services: [],
            filterId: 'all',
            highlight: instance => true,
            events: [],
            eventsErrors: []
        };
        this.connect = this.connect.bind(this);
        this.switch = this.switch.bind(this);
        this.retrieveEnvs = this.retrieveEnvs.bind(this);
        this.retrieveInstances = this.retrieveInstances.bind(this);

        console.info("envApp !constructor");
    }
    connect() {
        this.sock = new SockJS('/realtime');

        const thisComponent = this;
        //var eventsDiv = this.eventsList;
        this.sock.onopen = function() {
            console.log('open');
            thisComponent.setState({realtime: true});
            ///eventsDiv.innerHTML = eventsDiv.innerHTML + 'Open<br>';
            //sock.send('test');
            thisComponent.state.events.unshift({type: 'notification', message: 'Open'});
            thisComponent.setState({events: thisComponent.state.events});
        };

        this.sock.onmessage = function(e) {
            //var r = Math.floor((Math.random() * 3) + 1);
            //var fakeStatus = r == 1 ? 'LIVE' : r == 2 ? 'LOADING' : 'DOWN';
            let event = JSON.parse(e.data);
            console.log('real time event', event, thisComponent.state);
            //eventsDiv.innerHTML = eventsDiv.innerHTML + ' ' + event.env + ' ' + event.name + ' ' +
            //    event.status + ' ' + event.leader + ' ' + event.participating + '<br>';
            thisComponent.state.events.unshift({type: 'status', message: event.env + ' ' + event.name + ' ' + event.status + ' ' + event.leader + ' ' + event.participating})
            thisComponent.setState({events: thisComponent.state.events});

            // FIXME review how to identify services/instances
            var found = thisComponent.state.services.find(svc => svc.instances.find(ins => ins.env == event.env && ins.name == event.name));

            if (found) {
                found = found.instances.find(ins => ins.name == event.name);
                //console.info("found", found);

                found.status = event.status;
                found.leader = event.leader;
                found.participating = event.participating;
                thisComponent.setState({services: thisComponent.state.services});
            }
        };

        this.sock.onclose = function() {
            console.log('close');
            thisComponent.setState({realtime: false});
            //eventsDiv.innerHTML = eventsDiv.innerHTML + 'Close<br>';
            thisComponent.state.events.unshift({type: 'notification', message: 'Close'});
            thisComponent.setState({events: thisComponent.state.events});
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
    retrieveEnvs() {
        const thisComponent = this;
        axios.get('/data/envs')
            .then(response => {
                console.log("envApp data envs", response.data);
                thisComponent.setState({envs: response.data});
            })
            .catch(function (error) {
                console.log("envApp error envs", error.response);
                thisComponent.setState({envs: []});
            });
    }
    retrieveInstances(env) {
        console.log("Retr... " + env);
        const thisComponent = this;
        axios.get('/data/instances/' + env + '/services')
            .then(response => {
                console.log("envApp data services", response.data);
                thisComponent.setState({services: response.data});
            })
            .catch(function (error) {
                console.log("envApp error services", error);
                thisComponent.setState({services: []});
            });
    }
    retrieveEvents(env) {
        console.log("Retr events... " + env);
        const thisComponent = this;
        axios.get('/data/events/' + env)
            .then(response => {
                console.log("envApp data events", response.data);
                thisComponent.setState({events: response.data.events});
                thisComponent.setState({eventsErrors: response.data.errors});
            })
            .catch(function (error) {
                console.log("envApp error events", error);
                thisComponent.setState({events: []});
                thisComponent.setState({eventsErrors: ["Could not retrieve data"]});
            });
    }
    reloadInventory() {
        const thisComponent = this;
        axios.post('/data/instances/' + this.state.env + '/reload-inventory')
            .then(response => {
                console.log("reloaded inv", response.data);
                this.retrieveInstances(thisComponent.state.env);
            })
            .catch(function (error) {
                console.log("reload error inv", error.response);
            });
    }
    reloadScm() {
        const thisComponent = this;
        axios.post('/data/instances/' + this.state.env + '/reload-scm')
            .then(response => {
                console.log("reloaded", response.data);
                this.retrieveInstances(thisComponent.state.env);
            })
            .catch(function (error) {
                console.log("reload error", error.response);
            });
    }
    reloadSsh() {
         const thisComponent = this;
         axios.post('/data/instances/' + this.state.env + '/reload-ssh')
            .then(response => {
                console.log("reloaded", response.data);
                this.retrieveInstances(thisComponent.state.env);
            })
            .catch(function (error) {
                console.log("reload error", error.response);
            });
    }
    componentWillMount() {
        console.info("envApp !will-mount ");
    }
    componentWillReceiveProps(nextProps) {
        const newEnv = nextProps.match.params.id;
        console.info("envApp !props-chg ", this.props.match.params.id, newEnv);
        this.setState({env: newEnv});
        if (this.props.match.params.id != newEnv) {
            this.retrieveInstances(newEnv);
            this.retrieveEvents(newEnv);
        }
    }
    componentDidMount() {
        console.info("envApp !mount ");
        //this.connect();
        this.retrieveEnvs();
        this.retrieveInstances(this.state.env);
        this.retrieveEvents(this.state.env);
    }
    selectEnv(env) {
        this.setState({env: env});
    }
    changeSelected(filterId, highlightFunction) {
        console.log('this is:', filterId, highlightFunction);
        this.setState({filterId: filterId, highlight: highlightFunction})
    }
    render() {
        const envsByType = groupBy(this.state.envs, 'type');
        const services = this.state.services;
        const instances = flatMap(services, svc => svc.instances);
        const selectedInstances = instances.filter (i => i.id === this.props.match.params.instanceId);

        const systems = extendSystems(flatMap(services, p => p.service.systems));

        const selectedServices = services.filter(s => matchesList(extendSystems(s.service.systems), this.state.selectedSystems));

        console.info(instances, selectedInstances);
        const events = this.state.events;
        const eventsErrors = this.state.eventsErrors;

        const iterable = instances.map(instance => instance.host);
        const hosts = [...new Set(iterable)].sort();

        console.info("envApp !render");

        // {this.state.env}
        const title = (
            <span>
                Environment
                &nbsp;&nbsp;&nbsp;
                {Object.keys(envsByType).map(type =>
                    <span key={type}>
                        <span>{type} </span>
                        {envsByType[type].map(env =>
                            <Link key={env.env} to={"/envs/" + env.env}>
                                <button style={{color: this.state.env == env.env ? 'gray' : null}} onClick={e => this.selectEnv(env.env)}>{env.env}</button>
                            </Link>
                        )}
                        &nbsp;
                    </span>
                )}
            </span>
        );

        return (
            <AppLayout title={title}>
                <div className='main-content'>
                    <div className='principal-content'>
                        <Container>
                            <EnvironmentLogical services={selectedServices} highlight={this.state.highlight} />
                        </Container>
                    </div>
                    <div className='side-content'>
                        <div className='side-content-wrapper'>
                            <div className='side-primary'>
                                Systems:
                                <TagCloud tags={systems} displayCount={true}
                                          selectionChanged={values => this.setState({selectedSystems: values})} />
                                <InstanceFilter id={this.state.filterId} hosts={hosts} onFilterChanged={(e, hf) => this.changeSelected(e, hf)} />
                                <button onClick={e => this.reloadInventory()}>RELOAD INV</button>
                                <button onClick={e => this.reloadScm()}>RELOAD SCM</button>
                                <button onClick={e => this.reloadSsh()}>RELOAD SSH</button>
                                <Toggle toggled={this.state.realtime} onToggleChanged={this.switch} />
                                <div>
                                    {selectedInstances.map(i =>
                                        <Well block>
                                            <h3>{i.id} properties:</h3>
                                            <InstanceProperties properties={i.properties} />
                                        </Well>
                                    )}
                                </div>
                            </div>
                            <div className='side-secondary'>
                                <Container>
                                    <Events events={events} errors={eventsErrors} />
                                </Container>
                            </div>
                        </div>
                    </div>

                </div>
            </AppLayout>
        );
    }
}

// FIXME Duplicated
function matchesList(componentValues, selectedValues) {
    if (selectedValues && selectedValues.length > 0) {
        const values = (componentValues||[]);
        return (selectedValues||[]).find(value => values.indexOf(value) !== -1);
    }
    return true
}

export { EnvApp }