import React from "react";
import SockJS from "sockjs-client";
import axios from "axios/index";
import {Container, groupBy, Toggle} from "../common/component-library";
import {EnvironmentLogical, InstanceFilter} from "../environment/env";
import {AppLayout} from "./common/layout";
import {Link} from "react-router-dom";

class EnvApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {env: props.match.params.id, envs: [], realtime: false, instances: [], filterId: 'all', highlight: instance => true};
        this.connect = this.connect.bind(this);
        this.switch = this.switch.bind(this);
        this.retrieveEnvs = this.retrieveEnvs.bind(this);
        this.retrieveInstances = this.retrieveInstances.bind(this);

        console.info("envApp !constructor");
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
            console.log('real time event', event);
            eventsDiv.innerHTML = eventsDiv.innerHTML + ' ' + event.env + ' ' + event.name + ' ' +
                event.status + ' ' + event.leader + ' ' + event.participating + '<br>';

            // FIXME review how to identify instances
            const found = thisComponent.state.instances.find(instance => instance.name == event.name);

            //console.info("found", found);

            if (found) {
                found.status = event.status;
                found.leader = event.leader;
                found.participating = event.participating;
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
        axios.get('/data/instances/' + env)
            .then(response => {
                console.log("envApp data instances", response.data);
                thisComponent.setState({instances: response.data});
            })
            .catch(function (error) {
                console.log("envApp error", error);
                thisComponent.setState({instances: []});
            });
    }
    reload() {
        axios.post('/data/instances/' + this.state.env + '/reload')
            .then(response => {
                console.log("reloaded", response.data);
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
        }
    }
    componentDidMount() {
        console.info("envApp !mount ");
        //this.connect();
        this.retrieveEnvs();
        this.retrieveInstances(this.state.env);
    }
    selectEnv(env) {
        this.setState({env: env});
    }
    changeSelected(filterId, highlightFunction) {
        console.log('this is:', filterId, highlightFunction);
        this.setState({filterId: filterId, highlight: highlightFunction})
    }
    render() {
        const instancesByEnv = groupBy(this.state.instances, 'env');
        const instances = instancesByEnv[this.state.env] || [];
        const envsByType = groupBy(this.state.envs, 'type');
        //console.info("envApp", this.state.instances, this.state.env, instances);

        const iterable = instances.map(instance => instance.host);
        const hosts = Array.from(new Set(iterable)).sort();

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
                <div style={{display: 'table', width: '100%', height: '100%', boxSizing: 'border-box', tableLayout: 'fixed', borderSpacing: '0px 0px'}}>
                    <div style={{display: 'table-cell', width: '80%', height: '100%', boxSizing: 'border-box', verticalAlign: 'top', overflow: 'hidden', padding: '4px'}}>
                        <Container>
                            <EnvironmentLogical instances={instances} highlight={this.state.highlight} />
                        </Container>
                    </div>
                    <div style={{display: 'table-cell', width: '20%', height: '100%', boxSizing: 'border-box', verticalAlign: 'top', overflow: 'hidden', padding: '4px'}}>
                        <Container>
                            <InstanceFilter id={this.state.filterId} hosts={hosts} onFilterChanged={(e, hf) => this.changeSelected(e, hf)} />
                            <button onClick={e => this.reload()}>
                                RELOAD
                            </button>
                            <Toggle toggled={this.state.realtime} onToggleChanged={this.switch} />
                            <div ref={node => this.eventsList = node}></div>
                        </Container>
                    </div>
                </div>
            </AppLayout>
        );
    }
}

export { EnvApp }