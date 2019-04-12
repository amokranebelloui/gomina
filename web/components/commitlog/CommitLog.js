// @flow
import * as React from "react";
import {Link} from "react-router-dom";
import type {VersionType} from "../common/Version";
import {Version, VersionLabel} from "../common/Version";
import {Badge} from "../common/Badge";

import './CommitLog.css'
import {DateTime} from "../common/DateTime";
import {Revision} from "./Revision";
import type {UserRefType} from "../misc/UserType";

type CommitType = {
    author?: ?UserRefType,
    date?: ?number,
    message?: ?string,
    version?: ?string,
    revision?: ?number,
    instances?: Array<InstanceRefType>,
    deployments?: Array<InstanceRefType>
}
type InstanceRefType = {
    id: string,
    env: number,
    name: string,
    running: VersionType,
    deployed: VersionType
}

type Props = {
    commits: Array<CommitType>,
    unresolved: Array<InstanceRefType>,
    //instances: Array<Instance>,
    type?: ?string
}
class CommitLog extends React.Component<Props> {
    render() {
        if (this.props.commits) {
            /*
            const sortedCommits = this.props.commits;
            const instances = this.props.instances;

            const logByRevision: { [number]: Line } = {};
            const unknown = [];
            indexByRevision(sortedCommits, instances, logByRevision, unknown);

            console.log("%%%", logByRevision);
            function keys<T: number>(obj: any): Array<T> {
                return Object.keys(obj)
            }
            const log = keys(logByRevision).sort((a, b) => b - a);
            */

            const log: Array<CommitType> = this.props.commits || [];
            const unresolved: Array<InstanceRefType> = this.props.unresolved || [];
            return (
                <div className="commit-log">
                    <div className="items">
                        {unresolved.map(instance => [
                            instance.running && <Badge key={instance.id} backgroundColor="#AABBAA">{instance.env} {instance.name} <VersionLabel version={instance.running} /></Badge>,
                            instance.deployed && <Badge key={instance.id} backgroundColor="#EEEEAA">{instance.env} {instance.name} <VersionLabel version={instance.deployed} /></Badge>
                        ]
                        )}
                    </div>
                    <table className="commit-log-table">
                        <tbody>
                        {log.map(commit =>
                            <tr block="true" key={commit.revision}>
                                <td style={{width: '30px'}}><Revision revision={commit.revision} type={this.props.type}></Revision></td>
                                <td style={{width: '20px'}}>{commit.author && <UserRef user={commit.author} /> || '-'}</td>
                                <td style={{width: '80px'}}>
                                    <DateTime date={commit.date}/>
                                </td>
                                <td style={{verticalAlign: 'middle'}}>
                                    <span style={{verticalAlign: 'middle'}}>
                                        {(commit.message || '') + ' ' || '-'}
                                    </span>
                                    <span className="items" style={{float: 'right'}}>
                                        {(commit.instances||[]).map(instance =>
                                            <Badge key={instance.id} backgroundColor="#AABBAA">{instance.env} {instance.name} <VersionLabel version={instance.running} /></Badge>
                                        )}
                                        {(commit.deployments||[]).map(instance =>
                                            <Badge key={instance.id} backgroundColor="#EEEEAA">{instance.env} {instance.name} <VersionLabel version={instance.deployed} /></Badge>
                                        )}

                                    </span>
                                    {commit.version &&
                                    <span className="items" style={{float: 'right'}}>
                                        <Badge><i style={{opacity: .4}}>{commit.version}</i></Badge>
                                    </span>
                                    }
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                    <div className="items">
                        <Badge backgroundColor="#AABBAA">Running</Badge>
                        <Badge backgroundColor="#EEEEAA">Deployed</Badge>
                    </div>
                </div>
            )
        }
        return null
    }
}

function UserRef(props: {user: UserRefType}) {
    return (
        <span>
            {props.user.shortName}
            {props.user.id && <Link to={"/user/" + props.user.id}>&nbsp;&rarr;</Link>}
        </span>
    )
}

// TODO Clean old client side matching of instances to commits
/*
type Instance = {
    id: string,
    env: string,
    name: string,
    version: string,
    revision: number,
    deployVersion: string,
    deployRevision: number,
}
*/
/*
type Line = {
    commit?: ?Commit,
    version2?: ?string,
    instances?: Array<Instance>,
    deployments?: Array<Instance>
}

function indexByRevision(sortedCommits: Array<Commit>, instances: Array<Instance>, logByRevision: { [number]: Line }, unknown: Array<Instance>) {
    const logByVersion = {};
    sortedCommits.map(commit => {
        const o: Line = {instances: [], deployments: [], commit: commit};
        if (commit.revision) {
            logByRevision[commit.revision] = o;
        }
        if (commit.version) {
            logByVersion[commit.version] = o;
        }
    });


    (instances||[]).forEach(i => {
        let added = false;
        if (i.revision) {
            logByRevision[i.revision] = logByRevision[i.revision] || {commit: {}};
            logByRevision[i.revision].version2 = i.version;
            added = addToInstances(logByRevision[i.revision], i);
        }
        if (!added && i.deployRevision) {
            logByRevision[i.deployRevision] = logByRevision[i.deployRevision] || {commit: {}};
            logByRevision[i.deployRevision].version2 = i.version;
            added = addToDeployments(logByRevision[i.deployRevision], i);
        }
        if (!added && i.version) {
            added = addToInstances(logByVersion[i.version], i);
        }
        if (!added && i.deployVersion && i.version != i.deployVersion) {
            added = addToDeployments(logByVersion[i.deployVersion], i);
        }
        if (!added) {
            unknown.push(i)
        }
    });

    //console.info(logByRevision);
}

function addToInstances(line: Object, i: Object) {
    if (line) {
        (line.instances = line.instances || []).push(i);
        return true
    }
    return false
}

function addToDeployments(line: Object, i: Object) { // Deployed but not yet running
    if (line) {
        (line.deployments = line.deployments || []).push(i);
        return true
    }
    return false
}
*/
export {CommitLog};
