// @flow
import React, {Fragment} from "react";
import {isSnapshotVersion, isStableVersion, sameVersions} from "../common/version-utils";
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

    hasUnreleasedChanges(i: InstanceType) {
        return i.latestVersion && (i.unreleasedChanges && i.unreleasedChanges > 0)
    }

    render() {
        const statusFilters = [
            {label: "LOADING", f: i =>
                    i.status === 'LOADING'
            },
            {label: "DOWN", f: i =>
                    i.status === 'DOWN' || i && i.status === 'OFFLINE'
            },
            {label: "RUNNING", f: i =>
                    i.status === 'LIVE'
            }
        ];

        const metadataFilters = [
            {label: "SNAPSHOTS", f: i =>
                    isSnapshotVersion(i.deployedVersion) || isSnapshotVersion(i.runningVersion)
            },
            {label: "RELEASED", f: i =>
                    isStableVersion(i.deployedVersion) || isStableVersion(i.runningVersion)
            },
            {label: "UNKNOWN_VERSION", f: i =>
                    !i.deployedVersion && !i.runningVersion
            },
            {label: "RELEASE_AVAILABLE", f: i =>
                    i.releasedVersion && i.deployedVersion && !sameVersions(i.deployedVersion, i.releasedVersion) ||
                    i.releasedVersion && i.runningVersion && !sameVersions(i.runningVersion, i.releasedVersion)
            },
            {label: "UNRELEASED_CHANGES", f: i =>
                    this.hasUnreleasedChanges(i) && !sameVersions(i.deployedVersion, i.latestVersion) ||
                    this.hasUnreleasedChanges(i) && !sameVersions(i.runningVersion, i.latestVersion)
            },
            {label: "UNEXPECTED", f: i =>
                    i.unexpected
            },
            {label: "WRONG HOST", f: i =>
                    i.unexpectedHost
            }
        ];
        const serviceFilters = [
            {label: "WITHOUT COMPONENT", f: svc =>
                    !svc || !svc.component
            }
        ];
        return (
            <Fragment>
                <h4>Filters</h4>
                <Selection id='ALL' label='ALL' selected={this.props.id}
                           onSelectionChanged={e => this.changeSelected('ALL', null)}/>
                <br/>

                <div style={{display: 'inline-block'}} className='items'>
                    {statusFilters.map(filter =>
                        <Selection key={filter.label} id={filter.label} label={filter.label} selected={this.props.id}
                                   onSelectionChanged={e => this.changeSelected(filter.label, i => i && filter.f(i) || false)}/>
                    )}
                </div>
                <br/>

                <div style={{display: 'inline-block'}} className='items'>
                    {metadataFilters.map(filter =>
                        <Selection key={filter.label} id={filter.label} label={filter.label} selected={this.props.id}
                                   onSelectionChanged={e => this.changeSelected(filter.label, i => i && filter.f(i) || false)}/>
                    )}
                    {serviceFilters.map(filter =>
                        <Selection key={filter.label} id={filter.label} label={filter.label} selected={this.props.id}
                                   onSelectionChanged={e => this.changeSelected(filter.label, (i, s) => filter.f(s))}/>
                    )}
                </div>
                <br/>

                <div style={{display: 'inline-block'}} className='items'>
                    <Selection key="no_host" id="NO HOST" label="NO HOST" selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected("NO HOST", i => i && (!i.host && !i.deployHost) || false)}/>
                    <Selection key="with_host" id="WITH HOST" label="WITH HOST" selected={this.props.id}
                               onSelectionChanged={e => this.changeSelected("WITH HOST", i => i && (!!i.host || !!i.deployHost) || false)}/>
                    {this.props.hosts.map(host =>
                        <Selection key={host} id={host} label={host} selected={this.props.id}
                                   onSelectionChanged={e => this.changeSelected(host, i => i && (i.host === host || i.deployHost === host) || false)}/>
                    )}
                </div>

            </Fragment>
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