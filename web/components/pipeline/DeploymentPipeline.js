
import React from 'react';

import {groupBy} from '../common/utils';
import {compareVersions} from "../common/version-utils";
import {Well} from "../common/Well";
import {Version} from "../common/Version";

class DeploymentPipeline extends React.Component {
    render() {
        const component = this.props.component;
        const instancesByVersion = groupBy(this.props.instances || [], 'version')
        instancesByVersion[component.latest] = instancesByVersion[component.latest] || [];
        instancesByVersion[component.released] = instancesByVersion[component.released] || [];
        const sortedVersions = Object.keys(instancesByVersion).sort(compareVersions);
        return (
            <Well block margin="0 0 2px 0">
                <div style={{display: 'inline-block', verticalAlign: 'top', minWidth: '50px'}}>
                    <b>{component.id}(label?)</b>&nbsp;
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

export {DeploymentPipeline}