// @flow
import * as React from "react";
import {isSnapshot} from "../common/version-utils";
import {Well} from "../common/Well";
import type {InstanceType} from "./Instance"
import type {ServiceType} from "./Service";

type HighlightFunctionType = (instance?: ?InstanceType, service?: ?ServiceType) => boolean;

type InstanceFilterProps = {
    id: ?string,
    hosts: Array<string>,
    onFilterChanged: (id: string, ?HighlightFunctionType) => void
}
class InstanceFilter extends React.Component<InstanceFilterProps> {

    changeSelected(id: string, highlightFunction: ?HighlightFunctionType) {
        console.log('filter change:', id, highlightFunction);
        //this.setState({id: id, highlight: highlightFunction});
        this.props.onFilterChanged && this.props.onFilterChanged(id, highlightFunction);
    }

    render() {
        return (
            <Well block>
                <h4>Filters</h4>
                <Selection id='all' label='ALL' selected={this.props.id}
                           onSelectionChanged={e => this.changeSelected('all', null)}/>
                <br/>

                <div style={{display: 'inline-block'}}>
                    <Selection id='loading' label='LOADING' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('loading', i => i && i.status === 'LOADING' || false)}/>
                    <Selection id='down' label='DOWN' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('down', i => i && i.status === 'DOWN' || i && i.status === 'OFFLINE' || false)}/>
                    <Selection id='running' label='RUNNING' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('running', i => i && i.status === 'LIVE' || false)}/>
                </div>
                <br/>

                <div style={{display: 'inline-block'}}>
                    <Selection id='snapshots' label='SNAPSHOTS' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('snapshots', i => i && isSnapshot(i.version) || false)}/>
                    <Selection id='released' label='RELEASED' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('released', i => i && !isSnapshot(i.version) || false)}/>
                    <Selection id='unexpected' label='UNEXPECTED' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('unexpected', i => i && i.unexpected || false)}/>
                    <Selection id='unexpectedHost' label='WRONG HOST' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('unexpectedHost', i => i && i.unexpectedHost || false)}/>
                    <Selection id='withoutComponent' label='WITHOUT COMPONENT' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('withoutComponent', (instance, service) => {
                                   console.info('FILTER ', instance, service, service && service.component);
                                   return !service || !service.component
                               })}/>

                </div>
                <br/>

                <div style={{display: 'inline-block'}}>
                    {this.props.hosts.map(host =>
                        <Selection key={host} id={host} label={host} selected={this.props.id}
                                   onSelectionChanged={e => this.changeSelected(host, i => i && i.host === host || false)}/>
                    )}
                </div>

            </Well>
        )
    }
}

type SelectionProps = {
    id: string,
    selected: ?string,
    label: string,
    onSelectionChanged: (e: SyntheticMouseEvent<>) => void
}
class Selection extends React.Component<SelectionProps> {
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

export {InstanceFilter}