// @flow
import * as React from "react";
import {isSnapshot} from "../common/version-utils";
import {Well} from "../common/Well";
import type {InstanceType} from "./Instance"

type InstanceFilterProps = {
    id: ?string,
    hosts: Array<string>,
    onFilterChanged: (id: string, (instance: InstanceType) => boolean) => void
}
class InstanceFilter extends React.Component<InstanceFilterProps> {

    changeSelected(id: string, highlightFunction: (instance: InstanceType) => boolean) {
        console.log('filter change:', id, highlightFunction);
        //this.setState({id: id, highlight: highlightFunction});
        this.props.onFilterChanged && this.props.onFilterChanged(id, highlightFunction);
    }

    render() {
        return (
            <Well block>
                <h4>Filters</h4>
                <Selection id='all' label='ALL' selected={this.props.id}
                           onSelectionChanged={e => this.changeSelected('all', instance => true)}/>
                <br/>

                <div style={{display: 'inline-block'}}>
                    <Selection id='loading' label='LOADING' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('loading', instance => instance.status == 'LOADING')}/>
                    <Selection id='down' label='DOWN' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('down', instance => instance.status == 'DOWN' || instance.status == 'OFFLINE')}/>
                    <Selection id='running' label='RUNNING' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('running', instance => instance.status == 'LIVE')}/>
                </div>
                <br/>

                <div style={{display: 'inline-block'}}>
                    <Selection id='snapshots' label='SNAPSHOTS' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('snapshots', instance => isSnapshot(instance.version))}/>
                    <Selection id='released' label='RELEASED' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('released', instance => !isSnapshot(instance.version))}/>
                    <Selection id='unexpected' label='UNEXPECTED' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('unexpected', instance => instance.unexpected)}/>
                    <Selection id='unexpectedHost' label='WRONG HOST' selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected('unexpectedHost', instance => instance.unexpectedHost)}/>
                </div>
                <br/>

                <div style={{display: 'inline-block'}}>
                    {this.props.hosts.map(host =>
                        <Selection key={host} id={host} label={host} selected={this.props.id}
                                   onSelectionChanged={e => this.changeSelected(host, instance => instance.host == host)}/>
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