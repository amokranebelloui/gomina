
import React from 'react';

import {groupBy, isSnapshot, Well, Badge, Status, StatusWrapper, CopyButton, Version} from './gomina';
import {BuildLink} from './project';

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
                <span>{instance.name}</span>&nbsp;
                {extra.redisMaster && <Badge title={'Master in ServiceRegistry'} backgroundColor='#FFD21C' color='white'>***</Badge>}
                &nbsp;

                {isSlave && (extra.redisMasterLink
                    ? <Badge title={'Link Up'} backgroundColor='#FCF6D4' color='white'>OK</Badge>
                    : <Badge title={'Link Down Since ' + extra.redisMasterLinkDownSince} backgroundColor='#FF2929' color='white'>KO</Badge>)
                }
                &nbsp;
                {extra.redisRW == 'rw' && <Badge backgroundColor='#969696' color='white' title="Read/Write">RW</Badge>}
                {extra.redisRW == 'ro' && <Badge backgroundColor='#DBDBDB' color='white' title="Read Only">RO</Badge>}
                &nbsp;

                <span title={'Role ' + extra.redisRole + ' ' + extra.redisSlaveCount + ' slaves'}>
                    {extra.redisRole} {extra.redisSlaveCount}
                </span>
                &nbsp;

                {!instance.confCommited && <Badge title={'Config in not Committed'} backgroundColor='darkred' color='white'>chg</Badge>}

                <Badge title={'Persistence Mode ' + extra.redisMode} backgroundColor={persistenceModeColor} color='white'>
                    {extra.redisMode}
                </Badge>

                <span title={extra.redisClientCount + ' clients'}>
                    {extra.redisClientCount}
                </span>
                <br/>
                {extra.redisHost}:{extra.redisPort} -> {extra.redisMasterHost}:{extra.redisMasterPort}
                <span title="Offset (Diff)" style={{fontSize: 9, color: 'gray'}}>
                    {extra.redisOffset} {isSlave && ('(' + extra.redisOffsetDiff + ')')}
                </span>
                <br/>

                <CopyButton value={instance.folder} />
                <span style={{fontSize: 10}}>{instance.host} {instance.folder}</span>
                <Version version={instance.version} revision={instance.revision}/>
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

                    &nbsp;
                    <Version version={instance.version} revision={instance.revision}/>
                    &nbsp;
                    {!instance.confCommited && <Badge title={'Config in not Committed'} backgroundColor='darkred' color='white'>chg</Badge>}

                    <BuildLink url={instance.project} /> {/* // TODO Project Build URL */}
                </div>
                <div style={{display: 'flex', fontSize: 10}}>
                    <span>{instance.host}</span>
                    &nbsp;
                    <CopyButton value={instance.folder} />
                    &nbsp;
                    <span>{instance.folder}</span>
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
