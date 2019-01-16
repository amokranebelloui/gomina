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
        let compVersion = (v.running || v.deployed);
        const dispReleased = compVersion && v.released && compareVersionsRevisions(compVersion.version, compVersion.revision, v.released.version, v.released.revision) < 0;
        const dispLatest = compVersion && v.latest && compareVersionsRevisions(compVersion.version, compVersion.revision, v.latest.version, v.latest.revision) < 0;

        let versionStyle = {color: 'white', backgroundColor: '#d47951'};
        let deployedStyle = {color: 'black', backgroundColor: 'lightgray'};
        let releasedStyle = dispReleased ? {color: 'black', backgroundColor: '#b8cfdb'} : {};
        let latestStyle = dispLatest ? {color: 'black', backgroundColor: '#c7e8f9'} : {};

        return (
            <span>
                {v.running
                    ? <Version context="Running" version={v.running.version} revision={v.running.revision} {...versionStyle} />
                    : <Version context="Not running" {...{color:'black', backgroundColor:'lightgray'}} />}
                {v.deployed && // FIXME Debug dispDeployed
                <Version context="Deployed" version={v.deployed.version} revision={v.deployed.revision} {...deployedStyle} />}
                {dispReleased &&
                <Version context="Released" version={v.released.version} revision={v.released.revision} {...releasedStyle} />}
                {dispLatest &&
                <Version context="Latest Snapshot" version={v.latest.version} revision={    v.latest.revision} {...latestStyle} />}
            </span>
        )
    }
}

export {Versions}
