import axios from "axios/index";
import {AppLayout} from "./common/layout";
import React from "react";
import {Badge} from "../common/component-library";

class HostsApp extends React.Component {


    constructor(props) {
        super(props);
        this.state = {hosts: []};
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

    componentDidMount() {
        this.retrieveHosts()
    }
    
    render()  {
        const hosts = this.state.hosts;
        return (
            <AppLayout title="Hosts">
                <div style={{display: 'flex'}}>
                {hosts.map(host =>
                    <Host key={host.host} host={host}></Host>
                )}
                </div>
            </AppLayout>
        )
    }
}

function Host(props) {
    const host = props.host;
    //border: '1px solid blue'
    return (
        <div style={{border: '1px solid gray', padding: '2px', minWidth: '80px'}}>
            <div>
                <h3 style={{display: 'inline-block'}}>{host.host}</h3>
                <div style={{float: 'right', verticalAlign: 'top'}}>
                    {host.unexpected && host.unexpected.length > 0 &&
                    <Badge backgroundColor='#EAA910' color='white'>{host.unexpected.length}</Badge>
                    }
                </div>
            </div>
            <div><i>&lt;{host.dataCenter}&gt;</i></div>
            <hr/>
            <div>
                {host.username} / {host.passwordAlias}
                {host.sudo && <span> ({host.sudo})</span>}
            </div>
            <div>
                {host.type} {host.group}
            </div>
            <div>
                {host.tags.map(tag =>
                    <span style={{color: 'blue'}}>{tag} </span>
                )}
            </div>
        </div>
    )
}

export {HostsApp}