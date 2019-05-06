// @flow

import React from "react";
import type {VersionType} from "../common/version-utils";
import {isSnapshotVersion, sameVersions} from "../common/version-utils";
import {Version} from "../common/Version";
import {Badge} from "../common/Badge";
import "../common/items.css"

type Props = {
    running: VersionType,
    deployed: VersionType,
    released: VersionType,
    latest: VersionType
}

class Versions extends React.Component<Props> {
    render() {
        const {running, deployed, released, latest} = this.props;
        const sameDeployedRunning = (running || deployed) && sameVersions(running, deployed);

        let compVersion = (running || deployed);
        const dispReleased = released && !sameVersions(compVersion, released);
        const dispLatest = latest && !sameVersions(compVersion, latest);

        let versionStyle = {color: 'black', backgroundColor: '#c9c9c9'};
        let deployedStyle = {color: 'white', backgroundColor: '#d0d0d0'};
        let releasedStyle = dispReleased ? {color: 'black', backgroundColor: '#b8cfdb'} : {};
        let latestStyle = dispLatest ? {color: 'black', backgroundColor: '#c7e8f9'} : {};

        return (
            <span className="items" style={{whiteSpace: 'nowrap'}}>
                {running &&
                    <Version context="Running" version={running.version} revision={running.revision}
                             simplify={false} displaySnapshotRevision={true} {...versionStyle} />
                }
                {deployed && !sameDeployedRunning &&
                    <Version context="Deployed" version={deployed.version} revision={deployed.revision}
                             simplify={false} displaySnapshotRevision={true} {...deployedStyle} />
                }

                {dispReleased && <Badge title={'Release ' + released.version} {...releasedStyle}>R</Badge> }
                {dispLatest && <Badge title={'Latest ' + latest.version} {...latestStyle}>L</Badge>}
            </span>
        )
    }
}

export {Versions}
