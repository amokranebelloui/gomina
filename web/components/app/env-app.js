import React from "react";
import SockJS from "sockjs-client";
import axios from "axios/index";
import {groupBy, Toggle} from "../gomina";
import {EnvironmentLogical} from "../environment/env";
import {AppLayout} from "../layout";

class EnvApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {env: 'PROD', envs: [], realtime: false, instances: this.props.instances};
        this.connect = this.connect.bind(this);
        this.switch = this.switch.bind(this);
        this.retrieveEnvs = this.retrieveEnvs.bind(this);

        console.info("envApp !constructor ", this.props.instances);
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
                console.log("data envs", response.data);
                thisComponent.setState({envs: response.data});
            })
            .catch(function (error) {
                console.log("error envs", error.response);
                thisComponent.setState({envs: []});
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
        console.info("envApp !props-chg ");
        this.setState({instances: nextProps.instances});
    }
    componentDidMount() {
        console.info("envApp !mount ");
        //this.connect();
        this.retrieveEnvs()
    }
    selectEnv(env) {
        this.setState({env: env});
    }
    render() {
        const instancesByEnv = groupBy(this.state.instances, 'env');
        const instances = instancesByEnv[this.state.env] || [];
        const envsByType = groupBy(this.state.envs, 'type');
        console.info("envApp", this.state.instances, this.state.env, instances);

        return (
            <AppLayout title={"Environment '" + this.state.env + "'"}>
                <div style={{float: 'right'}}>
                    <button onClick={e => this.reload()}>
                        RELOAD
                    </button>
                    <Toggle toggled={this.state.realtime} onToggleChanged={this.switch} />
                    <div ref={node => this.eventsList = node}></div>
                </div>

                {Object.keys(envsByType).map(type =>
                    <span key={type}>
                        <span>{type} </span>
                        {envsByType[type].map(env =>
                            <button key={env.env} style={{color: this.state.env == env.env ? 'gray' : null}} onClick={e => this.selectEnv(env.env)}>{env.env}</button>
                        )}
                        <br />
                    </span>
                )}
                <hr/>
                <EnvironmentLogical instances={instances} />
            </AppLayout>
        );
    }
}

export { EnvApp }