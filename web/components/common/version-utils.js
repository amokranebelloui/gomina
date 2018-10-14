// @flow

function isSnapshot(version: String) {
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
    return res != 0 ? res : (a.includes("-SNAPSHOT") ? 0 : 1) - (b.includes("-SNAPSHOT") ? 0 : 1);
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


export {isSnapshot, compareVersions, compareVersionsRevisions}