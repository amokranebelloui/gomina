import React from "react";
import {Version} from "../common/Version";
import {Badge} from "../common/Badge";
import PropTypes from 'prop-types';

import './CommitLog.css'
import {DateTime} from "../common/DateTime";

class CommitLog extends React.Component {

    indexByRevision(sortedCommits, instances, logByRevision, unknown) {
        const logByVersion = {};
        sortedCommits.map(commit => {
            const o = Object.assign({instances: [], deployments: []}, commit);
            if (commit.revision) {
                logByRevision[commit.revision] = o;
            }
            if (commit.version) {
                logByVersion[commit.version] = o;
            }
        });


        instances.forEach(i => {
            let added = false;
            if (i.revision) {
                logByRevision[i.revision] = logByRevision[i.revision] || {};
                logByRevision[i.revision].version2 = i.version;
                added = this.addToInstances(logByRevision[i.revision], i);
            }
            if (!added && i.deployRevision) {
                logByRevision[i.deployRevision] = logByRevision[i.deployRevision] || {};
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

        console.info(logByRevision);
    }

    addToInstances(line, i) {
        if (line) {
            (line.instances = line.instances || []).push(i);
            return true
        }
        return false
    }

    addToDeployments(line, i) { // Deployed but not yet running
        if (line) {
            (line.deployments = line.deployments || []).push(i);
            return true
        }
        return false
    }
    
    render() {
        const sortedCommits = this.props.commits;
        const instances = this.props.instances;

        const logByRevision = {};
        const unknown = [];
        this.indexByRevision(sortedCommits, instances, logByRevision, unknown);

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
                    {Object.keys(logByRevision).sort((a, b) => b - a).map(revision =>
                        <tr block key={revision}>
                            <td style={{width: '30px'}}>{revision + ' ' || '-'}</td>
                            <td style={{width: '20px'}}>{(logByRevision[revision].author || '') + ' ' || '-'}</td>
                            <td style={{width: '80px'}}>
                                <DateTime date={logByRevision[revision].date}/>
                            </td>
                            <td>
                                <span>
                                    {(logByRevision[revision].message || '') + ' ' || '-'}
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
                                {logByRevision[revision].version &&
                                    <span style={{float: 'right', verticalAlign: 'middle'}}><i style={{opacity: .5}}>{logByRevision[revision].version}</i></span>
                                }
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        )
    }
}

CommitLog.propTypes = {
    commits: PropTypes.array.isRequired,
    instances: PropTypes.array
};

export {CommitLog};
