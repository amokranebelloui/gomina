import {flatMap} from "../common/utils";


function extendSystems(systems) {
    if (systems) {
        return flatMap(systems, s => extendSystem(s))
    }
    return null
}

function extendSystem(system) {
    if (system !== null) {
        var ext = "";
        var separator = "";
        return flatMap(system.split('.'), s => {
            ext = ext + separator + s;
            separator = ".";
            return ext
        })
    }
    return null
}

export { extendSystem, extendSystems }