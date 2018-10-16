// @flow
import * as React from "react";
import {isSnapshot} from "./version-utils";
import {Badge} from "./Badge";

type Props = {
    version?: ?string,
    revision?: ?string | ?number,
}

type VersionType = {
    version: string,
    revision?: ?string | ?number,
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

// FIXME Refactor to use VersionLabel, rename?
class Version extends React.PureComponent<Props> {
    constructor(props: Props) {
        super(props)
    }
    render() {
        const simplifiedVersion = this.props.version
            ? this.props.version.replace("-SNAPSHOT", "-S")
            : "unknown";

        /*
         const defaultStylingFunction = (version => isSnapshot(version) ? {color: 'white', backgroundColor: '#c30014'} : null);
         const stylingFunction = props.styling || defaultStylingFunction;
         const style = stylingFunction(props.version) || {color: 'black', backgroundColor: 'lightgray'}
         */
        const style = {color: 'white', backgroundColor: 'purple'};

        //const color = isSnapshot(props.version) ? "white" : "black";
        //const backgroundColor = isSnapshot(props.version) ? "#c30014" : "lightgray";
        const revision = this.props.revision ? this.props.revision : "*";
        return (
            <Badge color={style.color} backgroundColor={style.backgroundColor} {...this.props}>
                <span title={revision} style={{userSelect: 'all'}}>{simplifiedVersion}</span>
                &nbsp;
                {isSnapshot && <span style={{fontSize: "7px"}}>({revision})</span>}
            </Badge>
        )
    }
}                     

export {Version, VersionLabel}
export type {VersionType}
