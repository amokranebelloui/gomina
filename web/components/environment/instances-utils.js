// @flow

import type {InstanceType} from "./Instance";
import {isRunning} from "./Status";

function score(instance: InstanceType) {
    let score = 0;
    if (isRunning(instance.status)) {
        score = score + 1;
    }
    if (instance.status === "LIVE") {
        score = score + 1;
    }
    if (instance.participating) {
        score = score + 3;
    }
    if (instance.leader) {
        score = score + 5;
    }
    return score
}

function sortInstances(instances: Array<InstanceType>) {
    return instances.sort((a, b) => score(a) < score(b) ? 1 : -1);
}

export { sortInstances }