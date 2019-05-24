import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import React from "react";
import {Well} from "../common/Well";
import Link from "react-router-dom/es/Link";
import {Badge} from "../common/Badge";
import {InlineAdd} from "../common/InlineAdd";
import Route from "react-router-dom/es/Route";
import {HostEditor} from "../environment/HostEditor";
import {Secure} from "../permission/Secure";
import {HostConnectivityEditor} from "../environment/HostConnectivityEditor";
import {OSFamily} from "../misc/OSFamily";

class HostsApp extends React.Component {


    constructor(props) {
        super(props);
        this.state = {
            hosts: [],
            hostId: this.props.match.params.id,
            hostEdition: false,
            hostEdited: null,
            hostConnectivityEdition: false,
            hostConnectivityEdited: null
        };
        this.retrieveHosts = this.retrieveHosts.bind(this);
        console.info("hostsApp !constructor ");
    }

    retrieveHosts() {
        console.log("hostsApp Retr Hosts ... ");
        const thisComponent = this;
        axios.get('/data/hosts')
            .then(response => {
                console.log("hostsApp data hosts", response.data);
                thisComponent.setState({hosts: response.data});
            })
            .catch(function (error) {
                console.log("hostsApp error", error);
                thisComponent.setState({hosts: []});
            });
    }

    reloadSsh() {
        const thisComponent = this;
        axios.post('/data/hosts/' + this.state.hostId + '/reload')
            .then(() => thisComponent.retrieveHosts())
            .catch((error) => console.log("reload error", error.response));
    }

    componentDidMount() {
        this.retrieveHosts()
    }

    componentWillReceiveProps(nextProps) {
        const newHostId = nextProps.match.params.id;
        this.setState({hostId: newHostId});
        if (this.props.match.params.id != newHostId && newHostId) {
            // do something with newHostId
            this.setState({"hostEdition": false, "hostConnectivityEdition": false});
        }
    }

    clearNewHostState() {
        this.setState({
            hostname: ""
        })
    }
    addHost() {
        return this.state.hostname
            ? axios.post('/data/hosts/add?hostId=' + this.state.hostname)
            : Promise.reject("Hostname is mandatory");
    };
    onHostAdded(hostname, history) {
        this.retrieveHosts();
        history.push("/host/" + hostname)
    };


    editHost() {
        this.setState({"hostEdition": true, "hostConnectivityEdition": false});
    }
    changeHost(host) {
        console.info("Host Changed", host);
        this.setState({"hostEdited": host});
    }
    cancelHostEdition() {
        this.setState({"hostEdition": false});
    }
    updateHost() {
        const thisComponent = this;
        const hostname = this.props.match.params.id;
        axios.put('/data/hosts/' + hostname + '/update', this.state.hostEdited)
            .then(() => thisComponent.retrieveHosts())
            .catch((error) => console.log("host update error", error.response));
        this.setState({"hostEdition": false}); // FIXME Display Results/Errors
    }

    editHostConnectivity() {
        this.setState({"hostConnectivityEdition": true, "hostEdition": false});
    }
    changeHostConnectivity(hostConnectivity) {
        console.info("Host Connectivity Changed", hostConnectivity);
        this.setState({"hostConnectivityEdited": hostConnectivity});
    }
    cancelHostConnectivityEdition() {
        this.setState({"hostConnectivityEdition": false});
    }
    updateHostConnectivity() {
        const thisComponent = this;
        const hostname = this.props.match.params.id;
        axios.put('/data/hosts/' + hostname + '/update/connectivity', this.state.hostConnectivityEdited)
            .then(() => thisComponent.retrieveHosts())
            .catch((error) => console.log("host connectivity update error", error.response));
        this.setState({"hostConnectivityEdition": false}); // FIXME Display Results/Errors
    }

    render()  {
        const hosts = this.state.hosts;
        const hostId = this.state.hostId;
        let host = hosts.find(h => h.host == hostId);
        return (
            <AppLayout title="Hosts">
                <PrimarySecondaryLayout>
                    <div style={{display: 'flex', flexFlow: 'row wrap'}}>
                        {hosts.map(host =>
                            <Host key={host.host} host={host}></Host>
                        )}
                    </div>
                    <div>
                        <Secure permission="host.add">
                            <Route render={({ history }) => (
                                <InlineAdd type="Host"
                                           action={() => this.addHost()}
                                           onEnterAdd={() => this.clearNewHostState()}
                                           onItemAdded={(host) => this.onHostAdded(host, history)}
                                           editionForm={() =>
                                               <div>
                                                   <input type="text" name="hostname" placeholder="Hostname"
                                                          value={this.state.hostname}
                                                          onChange={e => this.setState({hostname: e.target.value})} />
                                                   <br/>
                                               </div>
                                           }
                                           successDisplay={(data: any) =>
                                               <div>
                                                   <b>{data ? ("Added " + JSON.stringify(data)) : 'no data returned'}</b>
                                               </div>
                                           }
                                />
                            )} />
                        </Secure>

                        {host &&
                        <Well block>
                            <h3>Detail {host && host.host}</h3>
                            <div style={{float: 'right'}}>
                                <Secure permission="host.manage">
                                    <button onClick={() => this.editHost()}>Edit</button>
                                </Secure>
                                <Secure permission="host.manage">
                                    <button onClick={() => this.editHostConnectivity()}>Connectivity</button>
                                </Secure>
                            </div>

                            {!this.state.hostEdition && !this.state.hostConnectivityEdition &&
                            <div>
                                {host.managed &&
                                    <button onClick={() => this.reloadSsh()}>RELOAD SSH</button>
                                }
                                {host && <Host host={host}></Host>}
                                    <hr/>
                                    <b>Should not be there</b>
                                {host && host.unexpected && host.unexpected.length > 0 &&
                                    host.unexpected.map(f =>
                                        <div key={f}>{f}</div>
                                    )
                                }
                            </div>
                            }
                            {this.state.hostEdition &&
                            <div>
                                <HostEditor host={host} onChange={(hostname, h) => this.changeHost(h)} />
                                <hr/>
                                <button onClick={() => this.updateHost()}>Update</button>
                                <button onClick={() => this.cancelHostEdition()}>Cancel</button>
                            </div>
                            }

                            {this.state.hostConnectivityEdition &&
                            <div>
                                <HostConnectivityEditor host={host} onChange={(hostname, hc) => this.changeHostConnectivity(hc)} />
                                <hr/>
                                <button onClick={() => this.updateHostConnectivity()}>Update</button>
                                <button onClick={() => this.cancelHostConnectivityEdition()}>Cancel</button>
                            </div>
                            }

                        </Well>
                        }
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        )
    }
}

function Host(props) {
    const host = props.host;
    //border: '1px solid blue'
    return (
        <div style={{display: 'inline-bloc', border: '1px solid #DDDDDD', padding: '2px', margin: '2px', width: '200px'}}>
            <div>
                <div style={{float: 'right', verticalAlign: 'top'}}>
                    {host.unexpected && host.unexpected.length > 0 &&
                        <Badge backgroundColor='#EAA910' color='white'>{host.unexpected.length}</Badge>
                    }
                    {!host.managed &&
                        <Badge backgroundColor='#4479ce' color='white'>?</Badge>
                    }
                    <OSFamily osFamily={host.osFamily} />
                </div>
                <Link to={"/host/" + host.host}>
                    <b style={{display: 'inline'}}>{host.host}</b>
                </Link>
                &nbsp;
                <div style={{display: 'inline-block', verticalAlign: 'top'}}>

                </div>
            </div>
            <div>
                <i>&lt;{host.dataCenter}&gt;</i>
            </div>
            <hr style={{border: '1px solid #DDDDDD'}}/>
            <div>
                {host.username} / {host.passwordAlias}
                {host.sudo && <span> [{host.sudo}]</span>}
            </div>
            <div>
                <b>Type: </b>{host.type}
            </div>
            <div>
                <b>Group: </b>{host.group}
            </div>
            <div>
                <b>OS: </b>{host.osFamily} / {host.os}
            </div>
            <div>
                <b>Tags: </b>
                {host.tags.map(tag =>
                    <span key={tag}>{tag} </span>
                )}
            </div>
        </div>
    )
}

export {HostsApp}