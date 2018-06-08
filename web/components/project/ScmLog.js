import React from "react";
import {Badge, groupBy, Well} from "../common/component-library";
import {Version} from "../common/version";

class ScmLog extends React.Component {
    render() {
        //const project = this.props.project;
        const sortedCommits = this.props.commits;
        const instances = this.props.instances;
        
        const logByRevision = {};
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

        const unknown = [];

        function addToInstances(line, i) {
            if (line) {
                (line.instances = line.instances || []).push(i);
                return true
            }
            return false
        }
        function addToDeployments(line, i) { // Deployed but not yet running
            if (line) {
                (line.deployments = line.deployments || []).push(i);
                return true
            }
            return false
        }

        instances.forEach(i => {
            console.info('aaa--->', i);
            let added = false;
            if (i.revision) {
                logByRevision[i.revision] = logByRevision[i.revision] || {};
                logByRevision[i.revision].version2 = i.version;
                added = addToInstances(logByRevision[i.revision], i);
            }
            if (!added && i.deployRevision) {
                logByRevision[i.deployRevision] = logByRevision[i.deployRevision] || {};
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

        console.info(logByRevision);
        return (
            <div>
                <b>SCM Log</b>
                <br/>
                {unknown.map(instance =>
                    <Badge key={instance.id} backgroundColor="#EEEEAA">
                        {instance.env} {instance.name}&nbsp;
                    </Badge>
                )}
                {Object.keys(logByRevision).sort((a, b) => b - a).map(revision =>
                    <Well block key={revision}>
                        <div style={{overflow: 'auto'}}>
                            <span style={{float: 'left', marginRight: '2px'}}>
                                {revision + ' ' || '-'}
                                {logByRevision[revision].author + ' ' || '-'}
                                {new Date(logByRevision[revision].date).toLocaleDateString() + ' ' || '-'}
                                {new Date(logByRevision[revision].date).toLocaleTimeString() + ' ' || '-'}
                            </span>
                            <span style={{
                                display: 'block',
                                marginLeft: '60px',
                                minHeight: '20px',
                                paddingLeft: '1px',
                                paddingRight: '1px'
                            }}>
                                {logByRevision[revision].message + ' ' || '-'}

                                <span style={{float: 'right', position: 'top'}}>
                                    {logByRevision[revision].version && <i style={{opacity: .5}}>{logByRevision[revision].version}</i>}
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
                            </span>

                        </div>
                    </Well>
                )}
            </div>
        )
    }
}

export {ScmLog};
