
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


export {isSnapshot, compareVersions, compareVersionsRevisions}