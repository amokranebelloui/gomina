import React from "react";
import {groupBy, Well} from "../common/component-library";
import {isSnapshot} from "../common/version";
import {AppInstance} from "./instance-app";
import {RedisInstance} from "./instance-redis";
import {Status} from "./instance-common";
import './env.css'
import {Service} from "./service";

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
        /*
        <span style={{verticalAlign: 'top', display: 'inline-block'}}>

                </span>
                <StatusWrapper status={instance.status}>
                            </StatusWrapper>


         */
        return ([
            <Status key={'status' + instance.id} status={instance.status} leader={instance.leader} participating={instance.participating} cluster={instance.cluster} style={{opacity: opacity}} />,
            <div key={instance.id} className='instance-badge' style={{display: 'table-cell', opacity:opacity}}>{comp}</div>
        ])
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

class InstanceFilter extends React.Component {
    changeSelected(id, highlightFunction) {
        console.log('filter change:', id, highlightFunction);
        //this.setState({id: id, highlight: highlightFunction});
        this.props.onFilterChanged && this.props.onFilterChanged(id, highlightFunction);
    }
    render() {
        return (
            <Well block>
                <h4>Filters</h4>
                <Selection id='all' label='ALL' selected={this.props.id}
                           onSelectionChanged={e => this.changeSelected('all', instance => true)} />
                <br/>

                <div style={{display: 'inline-block'}}>
                    <Selection id='loading' label='LOADING' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('loading', instance => instance.status == 'LOADING')} />
                    <Selection id='down' label='DOWN' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('down', instance => instance.status == 'DOWN' || instance.status == 'NOINFO')} />
                    <Selection id='running' label='RUNNING' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('running', instance => instance.status == 'LIVE')} />
                </div>
                <br/>

                <div style={{display: 'inline-block'}}>
                    <Selection id='snapshots' label='SNAPSHOTS' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('snapshots', instance => isSnapshot(instance.version))} />
                    <Selection id='released' label='RELEASED' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('released', instance => !isSnapshot(instance.version))} />
                    <Selection id='unexpected' label='UNEXPECTED' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('unexpected', instance => instance.unexpected)} />
                    <Selection id='unexpectedHost' label='WRONG HOST' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('unexpectedHost', instance => instance.unexpectedHost)} />
                </div>
                <br/>

                <div style={{display: 'inline-block'}}>
                    {this.props.hosts.map(host =>
                        <Selection key={host} id={host} label={host} selected={this.props.id}
                                   onSelectionChanged={e => this.changeSelected(host, instance => instance.host == host)} />
                    )}
                </div>

            </Well>
        )
    }
}

class EnvironmentLogical extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        const services = this.props.services;
        return (
            <div className='env-table'>
                {services.map( svc =>
                    <div key={svc.service.svc} className='env-row'>
                        <Service service={svc.service} instances={svc.instances || []} highlightFunction={this.props.highlight} />
                        {(svc.instances || []).map(instance =>
                            <Instance key={instance.id} instance={instance} highlighted={this.props.highlight(instance)} />
                        )}
                    </div>
                )}
            </div>
        )
    }
}

export {InstanceFilter, EnvironmentLogical}
