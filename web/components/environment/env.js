import React from "react";
import {Badge, groupBy, StatusWrapper} from "../common/component-library";
import {isSnapshot} from "../common/version";
import {AppInstance} from "./instance-app";
import {RedisInstance} from "./instance-redis";

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
