// @flow
import * as React from "react";
import type {VersionType} from "./version-utils";
import {isSnapshot} from "./version-utils";
import {Badge} from "./Badge";

type Props = {
    version?: ?VersionType,
    context?: ?string,
    simplify?: ?boolean,
    displaySnapshotRevision?: ?boolean
}

class VersionLabel extends React.PureComponent<{version: VersionType}> {
    render() {
        if (this.props.version) {
            const simplifiedVersion = this.props.version
                ? this.props.version.version.replace("-SNAPSHOT", "-S")
                : "unknown";
            const revision = this.props.version.revision ? this.props.version.revision : "*";
            return (
                <span title={revision} style={{userSelect: 'all'}}>
                {simplifiedVersion}
                    {isSnapshot(this.props.version.version) && <span style={{fontSize: "7px"}}>&nbsp;({revision})</span>}
            </span>
            )
        }
        return null
    }
}

type State = {
    displayRevision: boolean
}

// FIXME Refactor to use VersionLabel, rename?
class Version extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            displayRevision: false
        }
    }
    switchDisplay() {
        this.props.displaySnapshotRevision && this.setState({displayRevision: !this.state.displayRevision})
    }
    render() {
        if (this.props.version) {
            const {version, revision} = this.props.version;
            const snapshot = isSnapshot(version);
            const versionNumber = version.replace("-SNAPSHOT", "");
            const snapshotQualifier = snapshot ? (this.props.simplify ? "-S" : "-SNAPSHOT") : "";

            const style = {color: 'black', backgroundColor: 'lightgray'};

            return (
                <Badge color={style.color} backgroundColor={style.backgroundColor} {...this.props}>
                    <span title={(this.props.context||'') + " rev:" + (revision||'?')} style={{userSelect: 'all', whiteSpace: 'nowrap'}}>
                        <span>{versionNumber}</span>
                        {snapshot && <span onDoubleClick={() => this.switchDisplay()}>{snapshotQualifier}</span>}
                    </span>
                    {snapshot && this.state.displayRevision && <span style={{fontSize: "7px"}}>{revision}</span>}
                </Badge>
            )
        }
        return null
    }
}                     

export {Version, VersionLabel}
