// @flow

import * as React from "react";
import {compareVersions, compareVersionsRevisions} from "../common/version-utils";
import {Version} from "../common/Version";

type VersionType = {
    version?: ?string,
    revision?: ?number
}

type Props = {
    versions: {
        running: VersionType,
        deployed: VersionType,
        released: VersionType,
        latest: VersionType
    }
}

class Versions extends React.Component<Props> {
    render() {
        const v = this.props.versions || {};
        const dispDeployed = v.running && v.deployed && ((v.deployed.version && v.deployed.revision)
            ? compareVersionsRevisions(v.running.version, v.running.revision, v.deployed.version, v.deployed.revision) != 0
            : v.deployed.version
                ? compareVersions(v.running.version, v.deployed.version)
                : false);
        const dispReleased = v.running && v.released && compareVersionsRevisions(v.running.version, v.running.revision, v.released.version, v.released.revision) < 0;
        const dispLatest = v.running && v.latest && compareVersionsRevisions(v.running.version, v.running.revision, v.latest.version, v.latest.revision) < 0;

        let versionStyle = {color: 'black', backgroundColor: 'lightgray'};
        let deployedStyle = {color: 'white', backgroundColor: '#d47951'};
        let releasedStyle = dispReleased ? {color: 'black', backgroundColor: '#b8cfdb'} : {};
        let latestStyle = dispLatest ? {color: 'black', backgroundColor: '#c7e8f9'} : {};

        return (
            <span>
                {v.running
                    ? <Version version={v.running.version} revision={v.running.revision} {...versionStyle} />
                    : <Version {...{color:'black', backgroundColor:'lightgray'}} />}
                {dispDeployed &&
                <Version version={v.deployed.version} revision={v.deployed.revision} {...deployedStyle} />}
                {dispReleased &&
                <Version version={v.released.version} revision={v.released.revision} {...releasedStyle} />}
                {dispLatest &&
                <Version version={v.latest.version} revision={v.latest.revision} {...latestStyle} />}
            </span>
        )
    }
}

export {Versions}
