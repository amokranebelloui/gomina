import React from "react";
import {Badge, groupBy, Well} from "../common/component-library";
import {Version} from "../common/version";

class ScmLog extends React.Component {
    render() {
        //const project = this.props.project;
        const sortedCommits = this.props.commits;
        const instances = this.props.instances;
        const instancesByRevision = groupBy(instances, 'revision');
        const log = {};
        sortedCommits.map(commit => {
            log[commit.revision] = Object.assign({instances: []}, commit);
        });
        Object.keys(instancesByRevision).map(revision => {
            log[revision] = log[revision] || {};
            log[revision].instances = instancesByRevision[revision] || [];
            log[revision].version2 = instancesByRevision[revision][0].version;
        });
        console.info(log);
        return (
            <div>
                <b>SCM Log</b>
                <br/>
                {Object.keys(log).sort((a, b) => b - a).map(revision =>
                    <Well block key={revision}>
                        <div style={{overflow: 'auto'}}>
                            <span style={{float: 'left', marginRight: '2px'}}>
                                {revision + ' ' || '-'}
                                {log[revision].author + ' ' || '-'}
                                {new Date(log[revision].date).toLocaleDateString() + ' ' || '-'}
                                {new Date(log[revision].date).toLocaleTimeString() + ' ' || '-'}
                            </span>
                            <span style={{
                                display: 'block',
                                marginLeft: '60px',
                                minHeight: '20px',
                                paddingLeft: '1px',
                                paddingRight: '1px'
                            }}>
                                {log[revision].message + ' ' || '-'}

                                <span style={{float: 'right', position: 'top'}}>
                                    {log[revision].version && <i style={{opacity: .5}}>{log[revision].version}</i>}
                                    {log[revision].version2 &&
                                    <span style={{paddingLeft: '2px', paddingRight: '2px'}}>
                                        <Version version={log[revision].version2}/>
                                    </span>
                                    }
                                    {log[revision].instances.map(instance =>
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
