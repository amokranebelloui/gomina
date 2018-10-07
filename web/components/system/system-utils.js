import {flatMap} from "../common/utils";


function extendSystems(systems) {
    if (systems) {
        return flatMap(systems, s => extendSystem(s))
    }
    return []
}

function extendSystem(system) {
    if (system) {
        var ext = "";
        var separator = "";
        return flatMap(system.split('.'), s => {
            ext = ext + separator + s;
            separator = ".";
            return ext
        })
    }
    return []
}

export { extendSystem, extendSystems }