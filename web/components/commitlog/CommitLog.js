// @flow
import * as React from "react";
import {Version} from "../common/Version";
import {Badge} from "../common/Badge";

import './CommitLog.css'
import {DateTime} from "../common/DateTime";
import {Revision} from "./Revision";

type Props = {
    commits: Array<Commit>,
    instances: Array<Instance>,
    type?: ?string
}

type Instance = {
    id: string,
    env: string,
    name: string,
    version: string,
    revision: number,
    deployVersion: string,
    deployRevision: number,
}
type Commit = {
    author?: ?string,
    date?: ?number,
    message?: ?string,
    version?: ?string,
    revision?: ?number
}
type Line = {
    commit?: ?Commit,
    version2?: ?string,
    instances?: Array<Instance>,
    deployments?: Array<Instance>
}

// FIXME Instance: id, env, name, version, revision, deployVersion, deployRevision
class CommitLog extends React.Component<Props> {

    indexByRevision(sortedCommits: Array<Commit>, instances: Array<Instance>, logByRevision: { [number]: Object }, unknown: Array<Instance>) {
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
                added = this.addToInstances(logByRevision[i.revision], i);
            }
            if (!added && i.deployRevision) {
                logByRevision[i.deployRevision] = logByRevision[i.deployRevision] || {commit: {}};
                logByRevision[i.deployRevision].version2 = i.version;
                added = this.addToDeployments(logByRevision[i.deployRevision], i);
            }
            if (!added && i.version) {
                added = this.addToInstances(logByVersion[i.version], i);
            }
            if (!added && i.deployVersion && i.version != i.deployVersion) {
                added = this.addToDeployments(logByVersion[i.deployVersion], i);
            }
            if (!added) {
                unknown.push(i)
            }
        });

        //console.info(logByRevision);
    }

    addToInstances(line: Object, i: Object) {
        if (line) {
            (line.instances = line.instances || []).push(i);
            return true
        }
        return false
    }

    addToDeployments(line: Object, i: Object) { // Deployed but not yet running
        if (line) {
            (line.deployments = line.deployments || []).push(i);
            return true
        }
        return false
    }
    
    render() {
        function keys<T: number>(obj: any): Array<T> {
            return Object.keys(obj)
        }

        if (this.props.commits) {
            const sortedCommits = this.props.commits;
            const instances = this.props.instances;

            const logByRevision: { [number]: Line } = {};
            const unknown = [];
            this.indexByRevision(sortedCommits, instances, logByRevision, unknown);

            console.log("%%%", logByRevision);

            return (
                <div className="commit-log">
                    <div style={{padding: '2px'}}>
                        {unknown.map(instance =>
                            <Badge key={instance.id} backgroundColor="#EEEEAA" style={{marginRight: '2px'}}>
                                {instance.env} {instance.name}&nbsp;
                            </Badge>
                        )}
                    </div>
                    <table className="commit-log-table">
                        <tbody>
                        {keys(logByRevision).sort((a, b) => b - a).map(revision =>
                            <tr block="true" key={revision}>
                                <td style={{width: '30px'}}><Revision revision={revision} type={this.props.type}></Revision></td>
                                <td style={{width: '20px'}}>{(logByRevision[revision].commit.author || '') + ' ' || '-'}</td>
                                <td style={{width: '80px'}}>
                                    <DateTime date={logByRevision[revision].commit.date}/>
                                </td>
                                <td>
                                    <span>
                                        {(logByRevision[revision].commit.message || '') + ' ' || '-'}
                                    </span>
                                    <span style={{float: 'right'}}>

                                        {logByRevision[revision].version2 &&
                                        <span style={{paddingLeft: '2px', paddingRight: '2px'}}>
                                                <Version version={logByRevision[revision].version2}/>
                                            </span>
                                        }
                                        {logByRevision[revision].instances.map(instance =>
                                            <span key={instance.id} style={{paddingLeft: '2px', paddingRight: '2px'}}>
                                                <Badge backgroundColor="#AABBAA">
                                                    {instance.env} {instance.name}&nbsp;
                                                </Badge>
                                                </span>
                                        )}
                                        {(logByRevision[revision].deployments||[]).map(instance =>
                                            <span key={instance.id} style={{paddingLeft: '2px', paddingRight: '2px'}}>
                                                <Badge backgroundColor="#EEEEAA">
                                                    {instance.env} {instance.name}&nbsp;
                                                </Badge>
                                                </span>
                                        )}
                                    </span>
                                    {logByRevision[revision].commit.version &&
                                    <span style={{float: 'right', verticalAlign: 'middle'}}>
                                        <i style={{opacity: .5}}>{logByRevision[revision].commit.version}</i>
                                    </span>
                                    }
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            )
        }
        return null
    }
}

export {CommitLog};
