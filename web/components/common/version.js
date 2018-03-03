import React from "react";
import {Badge} from "./component-library";

function isSnapshot(version) {
    return version ? version.includes("-SNAPSHOT") : false;
}

function compareVersionsRevisions (a, reva, b, revb) {
    let versions = compareVersions(a || '', b || '');
    return versions != 0 ? versions : (reva || 0) - (revb || 0);
}

function compareVersions (a, b) {
    a = a || '';
    b = b || '';
    var res = compareVersions2(a.replace("-SNAPSHOT", ""), b.replace("-SNAPSHOT", ""));
    return res != 0 ? res : (a.includes("-SNAPSHOT") ? 0 : 1) - (b.includes("-SNAPSHOT") ? 0 : 1);
}

function compareVersions2 (a, b) {
    var i, diff;
    var regExStrip0 = /(\.0+)+$/;
    var segmentsA = a.replace(regExStrip0, '').split('.');
    var segmentsB = b.replace(regExStrip0, '').split('.');
    var l = Math.min(segmentsA.length, segmentsB.length);

    for (i = 0; i < l; i++) {
        diff = parseInt(segmentsA[i], 10) - parseInt(segmentsB[i], 10);
        if (diff) {
            return diff;
        }
    }
    return segmentsA.length - segmentsB.length;
}


function Version(props) {
    const simplifiedVersion = props.version
        ? props.version.replace("-SNAPSHOT", "-S")
        : "unknown";

    /*
     const defaultStylingFunction = (version => isSnapshot(version) ? {color: 'white', backgroundColor: '#c30014'} : null);
     const stylingFunction = props.styling || defaultStylingFunction;
     const style = stylingFunction(props.version) || {color: 'black', backgroundColor: 'lightgray'}
     */
    const style = {color: 'white', backgroundColor: 'purple'};

    //const color = isSnapshot(props.version) ? "white" : "black";
    //const backgroundColor = isSnapshot(props.version) ? "#c30014" : "lightgray";
    const revision = props.revision ? props.revision : "*";
    return (
        <Badge color={style.color} backgroundColor={style.backgroundColor} {...props}>
            <span title={revision} style={{userSelect: 'all'}}>{simplifiedVersion}</span>
            &nbsp;
            {isSnapshot && <span style={{fontSize: "7px"}}>({revision})</span>}
        </Badge>
    )
}


export {Version, isSnapshot, compareVersions, compareVersionsRevisions}