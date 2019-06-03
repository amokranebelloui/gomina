// @flow

type VersionType = {
    version: string,
    revision?: ?string | ?number
}

function sameVersions(v1: ?VersionType, v2: ?VersionType): boolean {
    let c = compareVersions(v1 && v1.version, v2 && v2.version);
    if (c === 0 && v1 && isSnapshot(v1.version)) {
        return (v1 && v1.revision) === (v2 && v2.revision)
    }
    return c === 0;
}

function isStableVersion(version: ?VersionType) {
    return version && !isSnapshot(version.version)
}

function isSnapshotVersion(version: ?VersionType) {
    return version && isSnapshot(version.version)
}

function isStable(version: ?string) {
    return version && !isSnapshot(version)
}

function isSnapshot(version: ?string) {
    return version ? version.includes("-SNAPSHOT") : false;
}

function compareVersionsRevisions (a: ?string, reva: ?number, b: ?string, revb: ?number) {
    let versions = compareVersions(a || '', b || '');
    return versions != 0 ? versions : (reva || 0) - (revb || 0);
}

function compareVersions (a: ?string, b: ?string) {
    a = a || '';
    b = b || '';
    var res = compareVersions2(a.replace("-SNAPSHOT", ""), b.replace("-SNAPSHOT", ""));
    return res !== 0 ? res : (a.includes("-SNAPSHOT") ? 0 : 1) - (b.includes("-SNAPSHOT") ? 0 : 1);
}

function compareVersionsReverse (a: ?string, b: ?string) {
    return compareVersions(b, a)
}

function compareVersions2 (a: string, b: string) {
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


export {isSnapshot, isStable, isStableVersion, isSnapshotVersion, compareVersions, compareVersionsReverse, compareVersionsRevisions, sameVersions}
export type { VersionType }
