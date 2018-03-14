import React from "react";
import {compareVersions, compareVersionsRevisions, Version} from "../common/version";
import {Badge} from "../common/component-library";

function isRunning(status) {
    return status == 'LIVE' || status == 'LOADING';
}

class StatusWrapper extends React.Component {
    render() {
        const status = this.props.status;
        const color =
            status == 'LIVE' ? 'green' :
                status == 'LOADING' ? 'orange' :
                    status == 'DOWN' ? 'red' :
                        'lightgray';
        const background =
            status == 'LIVE' ? '#e8f4e9' :
                status == 'LOADING' ? '#f9edcf' :
                    status == 'DOWN' ? '#f8e8e3' :
                        '#ededed';
        //#F8F8F8
        return (
            <div style={{
                padding: '2px', margin: this.props.margin, display: this.props.block ? null : 'inline-block',
                backgroundColor: background, border: '3px solid ' + color, borderRadius: '5px'
            }}>
                {this.props.children}
            </div>
        )
    }
}

class Status extends React.Component {
    render() {
        const status = this.props.status;
        const leader = this.props.leader;
        const participating = this.props.participating;
        const cluster = this.props.cluster;
        const backgroundColor =
            status == 'LIVE' && leader ? '#00ad0e' :
            status == 'LIVE' && participating ? '#69876f' :
            status == 'LIVE' ? '#9aa89b' :
            status == 'LOADING' && leader ? '#ff8600' :
            status == 'LOADING' && participating ? '#ffdc92' :
            status == 'LOADING' ? '#ebe5c5' :
            status == 'DOWN' && leader ? 'red' :
                        'lightgray';
        /*
         const badge = (
         <span style={{padding: "3px",
         color: 'white', backgroundColor: backgroundColor,
         fontSize: '9px',
         borderRadius: "5px", display: "inline-block"}}>
         {status}
         </span>
         )

         <span style={{float: 'left'}}>
                    {this.props.children}
                </span>
         */

        let role = {text: '-', title: 'No information', opacity: 0.3};
        if (isRunning(status)) {
            if (cluster) {
                if (leader) {
                    role = {text: 'LEADER', title: 'Elected Leader', opacity: 0.9}
                }
                else if (participating) {
                    role = {text: 'PARTICIP', title: 'Contending for Leadership', opacity: 0.7}
                }
                else {
                    role = {text: 'STANDBY', title: 'Standby instance', opacity: 0.3}
                }
            }
            else {
                if (leader) {
                    role = {text: 'LEADER', title: 'Leader', opacity: 0.9, fontStyle: 'italic'}
                }
                else {
                    role = {text: 'NONE', title: 'Not leader', opacity: 0.3}
                }
            }
        }
        return (
            <div className='status' style={Object.assign(this.props.style, {backgroundColor: backgroundColor, color: 'white'})}>
                <div style={{display: 'table', width: '100%', boxSizing: 'border-box', padding: '3px'}}>

                    <div style={{display: 'table-row'}}>
                        <div style={{display: 'table-cell', textAlign: 'left'}}>{status}</div>
                    </div>
                    <div style={{display: 'table-row'}}>
                        <div style={{display: 'table-cell', textAlign: 'right'}}>
                            <span title={role.title} style={{fontSize: '7px', fontStyle: role.fontStyle, opacity: role.opacity}}>
                                {role.text}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

        );
    }
}

class ConfCommited extends React.Component {
    render() {
        const display = !(this.props.commited == true);
        return (
            display &&
            <Badge title={'Config in not Committed'}
                   backgroundColor='darkred' color='white'>{this.props.commited == false ? 'chg' : '?'}</Badge>
        );
    }
}

class Expected extends React.Component {
    render() {
        const display = !(this.props.expected == true);
        return (
            display &&
            <Badge title={'Unexpected'}
                   backgroundColor='gray' color='white'>'???'</Badge>
        );
    }
}

function Leader(props) {
    const leader = props.leader == true;
    const participating = props.participating == true;
    const cluster = props.cluster == true;

    let title;
    if (cluster) {
        title = leader ? 'Elected Leader' : participating ? 'Contending for Leadership' : 'Standby instance'
    }
    else {
        title = leader ? 'Leader' : 'Not leader'
    }
    return (
        <Badge title={title}
               backgroundColor={leader ? '#ff9a22' : participating ? 'gray' : 'lightgray'}
               border={cluster ? '#ff8f14' : undefined}
               color='white'>{leader ? '***' : '-'}</Badge>
    )
}

class RedisLink extends React.Component {
    render() {
        const status = this.props.status;
        const color = status == true ? 'green' : status == false ? 'red' : null;
        const title = status == false ? 'Link down since ' + this.props.since : 'Link Up';
        return (
            <Badge title={title} backgroundColor={color} color='white'>
                <b>&rarr;</b>
            </Badge>
        );
    }
}

class Host extends React.Component {
    render() {
        const unexpected = this.props.expected && this.props.expected != this.props.host;
        return (
            <span>
                <span title="Running on host" style={{userSelect: 'all'}}>{this.props.host}</span>
                {unexpected && <span title="Expected host" style={{marginLeft: '2px', userSelect: 'all', textDecoration: 'line-through'}}>{this.props.expected}</span>}
            </span>
        )
    }
}

class Port extends React.Component {
    render() {
        return (
            <span style={{userSelect: 'all'}}>{this.props.port}</span>
        )
    }
}

class Versions extends React.Component {
    render() {
        const instance = this.props.instance;
        const dispDeployed = (instance.deployVersion && instance.deployRevision)
            ? compareVersionsRevisions(instance.version, instance.revision, instance.deployVersion, instance.deployRevision) != 0
            : instance.deployVersion
                ? compareVersions(instance.version, instance.deployVersion)
                : false;
        const dispReleased = compareVersionsRevisions(instance.version, instance.revision, instance.releasedVersion, instance.releasedRevision) < 0;
        const dispLatest = compareVersionsRevisions(instance.version, instance.revision, instance.latestVersion, instance.latestRevision) < 0;

        let versionStyle = {color:'black', backgroundColor:'lightgray'};
        let deployedStyle = {color:'white', backgroundColor:'#d47951'};
        let releasedStyle = dispReleased ? {color:'black', backgroundColor:'#b8cfdb'} : {};
        let latestStyle = dispLatest ? {color:'black', backgroundColor:'#c7e8f9'} : {};

        return (
            <span>
                <Version version={instance.version} revision={instance.revision} {...versionStyle} />
                {dispDeployed && <Version version={instance.deployVersion} revision={instance.deployRevision} {...deployedStyle} />}
                {dispReleased && <Version version={instance.releasedVersion} revision={instance.releasedRevision} {...releasedStyle} />}
                {dispLatest && <Version version={instance.latestVersion} revision={instance.latestRevision} {...latestStyle} />}
            </span>
        )
    }
}

export { ConfCommited, Expected, Status, Leader, RedisLink, Host, Port, Versions, StatusWrapper};