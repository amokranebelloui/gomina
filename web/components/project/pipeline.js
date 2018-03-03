
import React from 'react';

import {groupBy, Well} from '../common/component-library';
import {compareVersions, Version} from "../common/version";

class ProjectPipeline extends React.Component {
    render() {
        const project = this.props.project;
        const instancesByVersion = groupBy(this.props.instances || [], 'version')
        instancesByVersion[project.latest] = instancesByVersion[project.latest] || [];
        instancesByVersion[project.released] = instancesByVersion[project.released] || [];
        const sortedVersions = Object.keys(instancesByVersion).sort(compareVersions);
        return (
            <Well block margin="0 0 2px 0">
                <div style={{display: 'inline-block', verticalAlign: 'top', minWidth: '50px'}}>
                    <b>{project.id}(label?)</b>&nbsp;
                </div>
                <div style={{display: 'inline-block'}}>
                    {sortedVersions.map(version =>
                        <div key={version} style={{display: 'inline-block', verticalAlign: 'top', padding: '4px'}}>
                            <Version version={version} /><br/>
                            {instancesByVersion[version].map(instance =>
                                <span key={instance.id}>{instance.env} {instance.service} {instance.name}<br/></span>
                            )}
                            <br/>
                        </div>
                    )}
                </div>
            </Well>
        )
    }
}

export {ProjectPipeline}