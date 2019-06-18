import React, {Fragment} from "react";
import SockJS from "sockjs-client";
import axios from "axios/index";
import {flatMap, groupBy, matchesList} from "../common/utils";
import type {EnvType} from "../environment/Environment";
import {EnvironmentLogical} from "../environment/Environment";
//import {Link} from "react-router-dom";
import {Toggle} from "../common/Toggle";
import {InstanceFilter} from "../environment/InstanceFilter";
import {Events} from "../environment/Events";
import type {InstanceType} from "../environment/Instance";
import {InstanceProperties} from "../environment/Instance";
import {Well} from "../common/Well";
import {extendSystems} from "../system/system-utils";
import {TagCloud} from "../common/TagCloud";
import Link from "react-router-dom/es/Link";
import {EnvInstances} from "../environment/EnvInstances";
import {AddEnvironment} from "../environment/AddEnvironment";
import {Secure} from "../permission/Secure";
import {AddService} from "../environment/AddService";
import {AddInstance} from "../environment/AddInstance";
import type {ServiceDetailType, ServiceType} from "../environment/Service";
import {EnvDetail} from "../environment/EnvDetail";
import {EnvEditor} from "../environment/EnvEditor";
import {ServiceDetail} from "../environment/ServiceDetail";
import {ServiceEditor} from "../environment/ServiceEditor";
import ls from "local-storage";
import {ApplicationLayout} from "./common/ApplicationLayout";
import {filterEvents} from "../environment/EventFilter";

class EnvApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            env: props.match.params.id || ls.get('env.selected'),
            envs: [],
            realtime: false,
            group: "SERVICES",
            services: [],
            filterId: 'all',
            highlight: null,

            envEdition: false,
            envEdited: null,
            serviceEdition: false,
            serviceEdited: null,

            filterEvents: [],
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
            thisComponent.state.events.unshift({type: 'status', message: event.env + ' ' + event.name + ' ' + event.status + ' ' + event.leader + ' ' + event.participating});
            thisComponent.setState({events: thisComponent.state.events});

            // FIXME review how to identify services/instances
            let found = thisComponent.state.services.find(svc => svc.instances.find(ins => ins.env === event.env && ins.name === event.name));

            if (found) {
                found = found.instances.find(ins => ins.name === event.name);
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
        if (this.state !== newStatus) {
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
            .then(response => thisComponent.setState({envs: response.data}))
            .catch(error => thisComponent.setState({envs: []}));
    }
    retrieveComponentRefs() {
        const thisComponent = this;
        axios.get('/data/components/refs')
            .then(response => thisComponent.setState({components: response.data}))
            .catch(error => thisComponent.setState({components: []}));
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
        axios.get('/data/events/env/' + env)
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
    reloadEvents() {
        const thisComponent = this;
        axios.post('/data/events/reload-events')
            .then(() => this.retrieveEvents(thisComponent.state.env))
            .catch((error) => console.log("reload error", error.response))
    }

    enableEnv(env) {
        axios.put('/data/envs/' + env + '/enable')
            .then(() => this.retrieveEnvs())
            .catch((error) => console.log("env enable error", error.response));
    }
    disableEnv(env) {
        axios.put('/data/envs/' + env + '/disable')
            .then(() => this.retrieveEnvs())
            .catch((error) => console.log("env disable error", error.response));
    }
    deleteEnv(env) {
        axios.delete('/data/envs/' + env + '/delete')
            .then(() => this.retrieveEnvs())
            .catch((error) => console.log("env delete error", error.response));
    }

    deleteService(env, svc) {
        axios.delete('/data/instances/' + this.state.env + '/service/' + svc + '/delete')
            .then(() => this.retrieveInstances(env))
            .catch((error) => console.log("service delete error", error.response));
    }

    componentWillMount() {
        console.info("envApp !will-mount ");
    }
    componentWillReceiveProps(nextProps) {
        const newEnv = nextProps.match.params.id;
        console.info("envApp !props-chg ", this.props.match.params.id, newEnv);
        this.setState({env: newEnv});
        if (this.props.match.params.id !== newEnv) {
            document.title = newEnv + ' - Env';
            this.retrieveInstances(newEnv);
            this.retrieveEvents(newEnv);
        }
    }
    componentDidMount() {
        document.title = 'Env';
        console.info("envApp !mount ");
        //this.connect();
        this.retrieveEnvs();
        this.retrieveComponentRefs();
        this.retrieveInstances(this.state.env);
        this.retrieveEvents(this.state.env);
    }
    selectEnv(env) {
        this.setState({env: env});
        ls.set('env.selected', env);
    }
    changeSelected(filterId, highlightFunction) {
        console.log('this is:', filterId, highlightFunction);
        this.setState({filterId: filterId, highlight: highlightFunction})
    }

    editEnv() {
        this.setState({"envEdition": true});
    }
    changeEnv(env) {
        console.info("Env Changed", env);
        this.setState({"envEdited": env});
    }
    cancelEnvEdition() {
        this.setState({"envEdition": false});
    }
    updateEnv() {
        this.setState({"envEdition": false});
        axios.put('/data/envs/' + this.state.env + '/update', this.state.envEdited)
            .then(() => this.retrieveEnvs())
            .catch((error) => console.log("env update error", error.response));
    }

    serviceAdded(s) {
        this.retrieveInstances(this.state.env)
    }
    editService() {
        this.setState({"serviceEdition": true});
    }
    changeService(service) {
        console.info("Service Changed", service);
        this.setState({"serviceEdited": service});
    }
    changeServiceOrder(service, target) {
        console.info('Reordering ' + service + ' to ' + target);
        axios.put('/data/instances/' + this.state.env + '/service/' + service + '/reorder?target=' + target)
            .then(() => this.retrieveInstances(this.state.env))
            .catch((error) => console.log("service update error", error.response));
    }
    cancelServiceEdition() {
        this.setState({"serviceEdition": false});
    }
    updateService() {
        const svcId = this.props.match.params.svcId;
        axios.put('/data/instances/' + this.state.env + '/service/' + svcId + '/update', this.state.serviceEdited)
            .then(() => this.retrieveInstances(this.state.env))
            .catch((error) => console.log("service update error", error.response));
        this.setState({"serviceEdition": false}); // FIXME Display Results/Errors
    }

    instanceAdded(instanceId) {
        this.retrieveInstances(this.state.env)
    }
    deleteInstance(env, svc, instanceId) {
        axios.delete('/data/instances/' + this.state.env + '/service/' + svc + '/instance/' + instanceId + '/delete')
            .then(() => this.retrieveInstances(env))
            .catch((error) => console.log("instance delete error", error.response));
    }

    render() {
        const envsByType = groupBy(this.state.envs, 'type');
        const services = this.state.services;
        const instances = flatMap(services, svc => svc.instances);

        const selectedEnv = this.state.envs.find ((e: EnvType) => e.env === this.state.env);
        const manageable = selectedEnv && selectedEnv.active;
        const selectedInstances = instances.filter (i => i.id === this.props.match.params.instanceId);
        const svcId = this.props.match.params.svcId;
        const selectedServiceDetail: ServiceDetailType = services.find (s => s.service.svc === this.props.match.params.svcId);
        const selectedService: ServiceType = selectedServiceDetail && selectedServiceDetail.service;
        console.info("envApp !render", svcId, selectedService);

        const systems = extendSystems(flatMap(services, p => p.service.systems && p.service.systems.length > 0 ? p.service.systems : [""]));

        const selectedServices = services.filter(s => matchesList(extendSystems(s.service.systems), this.state.selectedSystems));

        console.info(instances, selectedInstances);
        const events = this.state.events;
        const eventsErrors = this.state.eventsErrors;

        const runningHosts = instances.map(instance => instance.host);
        const deployHosts = instances.map(instance => instance.deployHost);
        const hosts = [...new Set(runningHosts.concat(deployHosts).filter(h => h))].sort();

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
                                <button style={{color: this.state.env === env.env ? 'gray' : null}} onClick={() => this.selectEnv(env.env)}>{env.env}</button>
                            </Link>
                        )}
                        &nbsp;
                    </span>
                )}
            </span>
        );

        return (
            <ApplicationLayout title={title}
               header={() =>
                   <Fragment>
                       <b>{this.state.env}</b>
                       <div style={{float: 'right'}}>
                           <button
                               style={{color: this.state.group === 'SERVICES' ? 'gray' : null}}
                               onClick={() => this.setState({group: "SERVICES"})} >
                               SERVICES
                           </button>
                           <button
                               style={{color: this.state.group === 'INSTANCES' ? 'gray' : null}}
                               onClick={() => this.setState({group: "INSTANCES"})}>
                               INSTANCES
                           </button>
                       </div>
                       <div style={{clear: 'both'}}/>
                   </Fragment>
               }
               main={() =>
                   this.state.group === "INSTANCES" ?
                       <EnvInstances services={selectedServices} highlight={this.state.highlight} />
                       :
                       <EnvironmentLogical env={this.state.env}
                                           services={selectedServices}
                                           highlight={this.state.highlight}
                                           onOrderChange={(s, t) => this.changeServiceOrder(s, t)}
                       />
               }
               sidePrimary={() =>
                   <Fragment>
                       <InstanceFilter id={this.state.filterId} hosts={hosts} onFilterChanged={(e, hf) => this.changeSelected(e, hf)} />
                       <br/>
                       <b>Systems: </b>
                       <TagCloud tags={systems} displayCount={true}
                                 selectionChanged={values => this.setState({selectedSystems: values})} />
                       <br/>
                       <button onClick={() => this.reloadInventory()}>RELOAD INV</button>
                       <button onClick={() => this.reloadScm()}>RELOAD SCM</button>
                       <button onClick={() => this.reloadSsh()}>RELOAD SSH</button>
                       <button onClick={() => this.reloadEvents()}>RELOAD Events</button>
                       <Toggle toggled={this.state.realtime} onToggleChanged={this.switch} />
                       <br/>
                       <Secure permission="env.add">
                           <Well block>
                               <AddEnvironment onEnvironmentAdded={e => this.retrieveEnvs()} />
                           </Well>
                       </Secure>

                       {selectedEnv &&
                       <Well block>
                           <b>Env: {selectedEnv.env} </b>
                           <div style={{float: 'right'}}>
                               {manageable &&
                               <Secure permission="env.manage">
                                   <button onClick={() => this.editEnv()}>Edit</button>
                               </Secure>
                               }
                               {!selectedEnv.active &&
                               <Secure permission="env.manage">
                                   <button onClick={() => this.enableEnv(selectedEnv.env)}>Enable</button>
                               </Secure>
                               }
                               {selectedEnv.active &&
                               <Secure permission="env.manage">
                                   <button onClick={() => this.disableEnv(selectedEnv.env)}>Disable</button>
                               </Secure>
                               }
                           </div>
                           <br/>
                           {!this.state.envEdition &&
                           <div>
                               <EnvDetail env={selectedEnv} />
                               {!selectedEnv.active &&
                               <Secure permission="env.delete">
                                   <button onClick={() => this.deleteEnv(selectedEnv.env)}>Delete</button>
                               </Secure>
                               }
                               <br/>
                               {manageable &&
                               <Secure permission="env.manage">
                                   <AddService env={selectedEnv.env}
                                               components={this.state.components}
                                               onServiceAdded={s => this.serviceAdded(s)}/>
                               </Secure>
                               }
                           </div>
                           }
                           {this.state.envEdition &&
                           <div>
                               <EnvEditor env={selectedEnv} onChange={(id, e) => this.changeEnv(e)} />
                               <hr/>
                               <button onClick={() => this.updateEnv()}>Update</button>
                               <button onClick={() => this.cancelEnvEdition()}>Cancel</button>
                           </div>
                           }
                       </Well>
                       }

                       {svcId && selectedService &&
                       <Well block>
                           <b>Service: {svcId} </b>
                           {manageable &&
                           <div style={{float: 'right'}}>
                               <Secure permission="env.manage">
                                   <button onClick={() => this.editService()}>Edit</button>
                               </Secure>
                               <Secure permission="env.manage">
                                   <button onClick={() => this.deleteService(this.state.env, svcId)}>Delete</button>
                               </Secure>
                           </div>
                           }
                           <br/>
                           {!this.state.serviceEdition &&
                           <div>
                               <ServiceDetail service={selectedService} />
                               <br/>
                               {manageable &&
                               <Secure permission="env.manage">
                                   <AddInstance env={this.state.env} svc={svcId}
                                                onInstanceAdded={i => this.instanceAdded(i)}/>
                               </Secure>
                               }
                           </div>
                           }
                           {this.state.serviceEdition &&
                           <div>
                               <ServiceEditor components={this.state.components}
                                              service={selectedService}
                                              onChange={(svc, s) => this.changeService(s)} />
                               <hr/>
                               <button onClick={() => this.updateService()}>Update</button>
                               <button onClick={() => this.cancelServiceEdition()}>Cancel</button>
                           </div>
                           }
                       </Well>
                       }
                       <div>
                           {selectedInstances.map((i: InstanceType) =>
                               <Well key={i.id} block>
                                   <b>Instance: {i.id}</b>
                                   <div style={{float: 'right'}}>
                                       <Secure permission="env.manage">
                                           <button onClick={() => this.deleteInstance(this.state.env, svcId, i.name)}>Delete</button>
                                       </Secure>
                                   </div>
                                   <br/>
                                   {i.id}<br/>
                                   {i.name}<br/>
                                   <b>Host: </b>{i.deployHost}<br/>
                                   <b>Folder: </b>{i.deployFolder}<br/>
                                   <InstanceProperties properties={i.properties} />
                               </Well>
                           )}
                       </div>
                   </Fragment>
               }
               sideSecondary={() =>
                   <Fragment>
                       <b>Events</b>
                       <div style={{float: 'right'}}>
                           <input type="text" value={this.state.filterEvents}
                                  onChange={e => this.setState({filterEvents: e.target.value})} />
                       </div>
                       <Events events={filterEvents(events, this.state.filterEvents)} errors={eventsErrors} />
                       <div style={{clear: 'both'}} />
                   </Fragment>
               }
            />
        );
    }
}

export { EnvApp }