import React from "react";
import {groupBy, Badge, StatusWrapper, CopyButton} from "./gomina";
import {isSnapshot, compareVersions, compareVersionsRevisions, Version} from "./common/version";
import {BuildLink} from "./project";

class Instance extends React.Component {
    render() {
        const instance = this.props.instance;
        const opacity = this.props.highlighted ? 1 : 0.06;
        let comp;
        if (instance.type == 'redis') {
            comp = <RedisInstance instance={instance} />
        }
        else {
            comp = <AppInstance instance={instance} />
        }
        return (
            <div style={{display: "inline-block", verticalAlign: 'top', opacity:opacity}}>
                <StatusWrapper status={instance.status}>
                    {comp}
                </StatusWrapper>
            </div>
        )
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
                {unexpected && <span title="Expected host" style={{userSelect: 'all', textDecoration: 'line-through', marginLeft: '2px'}}>{this.props.expected}</span>}
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


class RedisInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        //let extra = instance.extra || {};
        let extra = instance || {}; // FIXME Extra
        const isSlave = extra.redisRole == 'SLAVE';
        //const isMaster = instance.extra.redisRole == 'MASTER';
        const persistenceModeColor = extra.redisMode == 'AOF' ? 'green' : extra.redisMode == 'RDB' ? 'lightgreen' : 'gray';
        /*<Status status={instance.status} />&nbsp;*/
        return (
            <div>
                <span style={{padding: "3px"}}><b>{instance.name}</b></span>
                &nbsp;

                {extra.redisRW == 'rw' && <Badge backgroundColor='#969696' color='white' title="Read/Write">RW</Badge>}
                {extra.redisRW == 'ro' && <Badge backgroundColor='#DBDBDB' color='white' title="Read Only">RO</Badge>}
                &nbsp;

                <Badge title={'Role ' + extra.redisRole + ' ' + extra.redisSlaveCount + ' slaves'}>
                    {extra.redisRole} {extra.redisSlaveCount}
                </Badge>
                &nbsp;
                <Badge title={extra.redisClientCount + ' clients'}>{extra.redisClientCount}</Badge>
                &nbsp;

                <ConfCommited commited={instance.confCommited} />
                <Expected expected={!instance.unexpected} />

                <Badge title={'Persistence Mode ' + extra.redisMode} backgroundColor={persistenceModeColor} color='white'>
                    {extra.redisMode}
                </Badge>

                <br/>

                <div style={{display: 'inline-block', marginTop: '3px'}}>
                    <span style={{display: 'inline-block', fontSize: 11, lineHeight: '10px', verticalAlign: 'middle'}}>
                        <span><Host host={extra.redisHost} expected={instance.deployHost} />:<Port port={extra.redisPort} /></span>
                        <br/>
                        <span style={{fontSize: 9, color: 'gray'}}>{extra.redisOffset}</span>
                    </span>
                    <span style={{display: 'inline-block', verticalAlign: 'middle', margin: '-4px 3px'}}>
                        {extra.redisMaster &&
                            /*
                            <Badge title={'Master in ServiceRegistry'}
                                   backgroundColor='#FFD21C'
                                   color='white'>***</Badge>*/
                            <Leader cluster={true} participating={true} leader={extra.redisMaster} />
                        }
                        {isSlave &&
                            <RedisLink status={extra.redisMasterLink} since={extra.redisMasterLinkDownSince}/>
                        }
                    </span>
                    {isSlave &&
                    <span style={{display: 'inline-block', fontSize: 11, lineHeight: '10px', verticalAlign: 'middle'}}>
                        <span>{extra.redisMasterHost}:{extra.redisMasterPort}</span><br/>
                        <span title="Offset (Diff)" style={{fontSize: 9, color: 'gray'}}>
                            {isSlave && ('(' + extra.redisOffsetDiff + ')')}
                        </span>
                    </span>
                    }
                </div>

                <br/>

                <span style={{userSelect: 'all', fontSize: 10}}>{instance.host}</span>&nbsp;
                <span style={{userSelect: 'all', fontSize: 10}}>{instance.deployFolder}</span>&nbsp;
                <CopyButton value={instance.deployFolder} />&nbsp;
                <Versions instance={instance} />
            </div>
        )
    }
}

class AppInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        //let extra = instance.extra || {};
        let extra = instance || {}; // FIXME Extra
        //, alignItems: 'top', justifyContent: 'center'
        /*<Status status={instance.status} />*/
        return (
            <div>
                <div>
                    <span>{instance.name}</span>
                    &nbsp;
                    <Leader leader={instance.leader} participating={instance.participating} cluster={instance.cluster} />

                    &nbsp;
                    <Versions instance={instance} />
                    &nbsp;
                    <ConfCommited commited={instance.confCommited} />
                    <Expected expected={!instance.unexpected} />

                    <BuildLink url={instance.project} /> {/* // TODO Project Build URL */}
                </div>
                <div style={{display: 'flex', fontSize: 10}}>
                    {instance.pid && <span>{instance.pid}@</span>}
                    <span><Host host={extra.host} expected={instance.deployHost} /></span>&nbsp;
                    <CopyButton value={instance.deployFolder} />&nbsp;
                    <span>{instance.deployFolder}</span>
                </div>
                <div>
                    <Badge>{extra.quickfixPersistence}</Badge>
                    <Badge>{extra.busVersion}</Badge>
                    <Badge>{extra.coreVersion}</Badge>
                    <Badge>{extra.jmx}</Badge>
                </div>
            </div>
        )
    }
}

class Service extends React.Component {
    render() {
        const instances = this.props.instances ? this.props.instances : [];
        const differentVersions = new Set(instances.map(instance => instance.version)).size > 1;
        const highlightFunction = this.props.highlightFunction || (instance => true);
        return (
            <div style={{padding: '2px'}}>
                <div style={{width: '140px', display: 'inline-block', verticalAlign: 'top', marginRight: '2px'}}>
                    <span><b>{this.props.name}</b></span>
                    <br/>
                    {differentVersions && <Badge backgroundColor="orange">versions?</Badge>}
                </div>
                {instances.map((instance) =>
                    <span key={instance.id} style={{marginLeft: '2px', marginRight: '2px'}}>
                            <Instance instance={instance} highlighted={highlightFunction(instance)}/>
                        </span>
                )}
            </div>
        )
    }
}

class Selection extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        return (
            <button style={{color: this.props.id == this.props.selected ? 'gray' : null}}
                    onClick={this.props.onSelectionChanged && this.props.onSelectionChanged}>{this.props.label}</button>
        )
    }
}

class EnvironmentLogical extends React.Component {
    constructor(props) {
        super(props);
        this.state = {id: 'all', highlight: instance => true}
    }
    changeSelected(id, highlightFunction) {
        console.log('this is:', id, highlightFunction);
        this.setState({id: id, highlight: highlightFunction})
    }
    render() {
        const instances = this.props.instances;
        const services = groupBy(instances, 'service');
        const iterable = instances.map(instance => instance.host);
        const hosts = Array.from(new Set(iterable)).sort();

        return (
            <div>

                <Selection id='all' label='ALL' selected={this.state.id}
                           onSelectionChanged={e => this.changeSelected('all', instance => true)} />

                <span>&nbsp;&nbsp;&nbsp;</span>

                <div style={{display: 'inline-block'}}>
                    <Selection id='loading' label='LOADING' selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected('loading', instance => instance.status == 'LOADING')} />
                    <Selection id='down' label='DOWN' selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected('down', instance => instance.status == 'DOWN')} />
                    <Selection id='running' label='RUNNING' selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected('running', instance => instance.status == 'LIVE')} />
                </div>

                <span>&nbsp;&nbsp;&nbsp;</span>

                <div style={{display: 'inline-block'}}>
                    <Selection id='snapshots' label='SNAPSHOTS' selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected('snapshots', instance => isSnapshot(instance.version))} />
                    <Selection id='released' label='RELEASED' selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected('released', instance => !isSnapshot(instance.version))} />
                    <Selection id='unexpected' label='UNEXPECTED' selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected('unexpected', instance => instance.unexpected)} />
                    <Selection id='unexpectedHost' label='WRONG HOST' selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected('unexpectedHost', instance => instance.unexpectedHost)} />
                </div>

                <span>&nbsp;&nbsp;&nbsp;</span>

                <div style={{display: 'inline-block'}}>
                    {hosts.map(host =>
                        <Selection key={host} id={host} label={host} selected={this.state.id}
                                   onSelectionChanged={e => this.changeSelected(host, instance => instance.host == host)} />
                    )}
                </div>

                <br/>

                {Object.keys(services).map( service =>
                    <Service key={service} name={service} instances={services[service]} highlightFunction={this.state.highlight} />
                )}
            </div>
        )
    }
}

export {EnvironmentLogical}
