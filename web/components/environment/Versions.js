import React from "react";
import {compareVersions, compareVersionsRevisions} from "../common/version-utils";

class Versions extends React.Component {
    render() {
        const instance = this.props.instance;
        const dispDeployed = (instance.deployVersion && instance.deployRevision)
            ? compareVersionsRevisions(instance.version, instance.revision, instance.deployVersion, instance.deployRevision) != 0
            : instance.deployVersion
                ? compareVersions(instance.version, instance.deployVersion)
                : false;
        const dispReleased = compareVersionsRevisions(instance.version, instance.revision, instance.releasedVersion, instance.releasedRevision) < 0;
        const dispLatest = compareVersionsRevisions(instance.version, instance.revision, instance.latestVersion, instance.latestRevision) < 0;

        let versionStyle = {color: 'black', backgroundColor: 'lightgray'};
        let deployedStyle = {color: 'white', backgroundColor: '#d47951'};
        let releasedStyle = dispReleased ? {color: 'black', backgroundColor: '#b8cfdb'} : {};
        let latestStyle = dispLatest ? {color: 'black', backgroundColor: '#c7e8f9'} : {};

        return (
            <span>
                <Version version={instance.version} revision={instance.revision} {...versionStyle} />
                {dispDeployed &&
                <Version version={instance.deployVersion} revision={instance.deployRevision} {...deployedStyle} />}
                {dispReleased &&
                <Version version={instance.releasedVersion} revision={instance.releasedRevision} {...releasedStyle} />}
                {dispLatest &&
                <Version version={instance.latestVersion} revision={instance.latestRevision} {...latestStyle} />}
            </span>
        )
    }
}

export {Versions}
