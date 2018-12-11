// @flow
import * as React from "react";
import {Badge} from "../common/Badge";
import {BuildLink} from "../build/BuildLink";
import type {InstanceType} from "./Instance";
import Link from "react-router-dom/es/Link";
import {InstanceBadge} from "./InstanceBadge";
import {sortInstances} from "./instances-utils";

type ServiceType = {
    svc: string,
    type?: ?string,
    mode?: ?string,
    activeCount?: ?number,
    componentId?: ?string,
    systems?: ?Array<string>,
}

type ServiceDetailType = {
    service: ServiceType,
    instances: Array<InstanceType>
}

type Props = {
    service: ServiceType,
    instances?: ?Array<InstanceType>,
}

class Service extends React.Component<Props> {
    render() {
        const service = this.props.service;
        const instances = this.props.instances ? sortInstances(this.props.instances) : [];
        const componentSet = new Set(instances.map(instance => instance.componentId).filter(p => p != null));
        const components = [...componentSet];
        //const status = computeStatus(service, instances);
        const d = computeServiceDetails(service, instances);
        return ([
            <td key={'detail' + service.svc} className="service" valign="middle">
                <span>
                    <b style={{fontSize: '16px'}}>{service.svc}</b>&nbsp;
                    {service.componentId &&
                        <Link to={'/component/' + service.componentId}>
                            <span>&rarr;</span>
                        </Link>
                    }
                    &nbsp;
                    <i>{service.type}</i>
                </span>&nbsp;
                <Badge backgroundColor="">{instances.length}</Badge>
                <span>{service.mode}</span>
                <span>|{service.systems}|</span>
                {components.map(component =>
                    <BuildLink key={component} url={'navigate/' + (component||'')}/>
                )}
                {d.unexpected && <Badge title="Unexpected instances running" backgroundColor="orange">exp?</Badge>}
                {d.versions && <Badge title="Different versions between instances" backgroundColor="orange">versions?</Badge>}
                {d.configs && <Badge title="Config not committed or different revisions between instances" backgroundColor="orange">conf?</Badge>}

                <div className="items" style={{display: 'table-cell', float: "right", height: 'auto'}}>
                    {instances.map( i =>
                        <InstanceBadge instance={i} />
                    )}
                </div>
            </td>
        ])
    }
}

function computeStatus(service: ServiceType, instances: Array<InstanceType>) {
    switch (service.mode) {
        case "ONE_ONLY": return computeStatusOnlyOne(service, instances);
        case "LEADERSHIP": return computeStatusLeadership(service, instances);
        case "LOAD_BALANCING": return computeStatusLoadBalancing(service, instances);
        case "OFFLINE": return computeStatusOffline(service, instances);
    }
    return {
        status: 'UNKNOWN',
        reason: 'UNKNOWN',
        text: 'Unknown mode ' + (service.mode || ''),
    }
}

function computeStatusOnlyOne(service: ServiceType, instances: Array<InstanceType>) {
    const loading = instances.filter(instance => instance.status === 'LOADING');
    const live = instances.filter(instance => instance.status === 'LIVE');
    const liveLeaders = instances.filter(instance => instance.status === 'LIVE' && instance.leader);
    const liveNotLeaders = instances.filter(instance => instance.status === 'LIVE' && !instance.leader);

    if (live.length > 1 || liveLeaders.length > 1) {
        return {status: 'ERROR', reason: 'MULTI', text: 'Multiple Instances Running'};
    }
    else if (liveLeaders.length === 1) {
        return {status: 'LIVE', reason: '-', text: '-'};
    }
    else if (loading.length > 0) {
        return {status: 'LOADING', reason: '-', text: '-'};
    }
    else if (liveNotLeaders.length > 0) {
        return {status: 'DOWN', reason: 'NO ACTIVE', text: 'No Active Instance'};
    }
    else {
        return {status: 'DOWN', reason: 'DOWN', text: 'No Running Instances'};
    }
}

function computeStatusLeadership(service: ServiceType, instances: Array<InstanceType>) {
    const loading = instances.filter(instance => instance.status === 'LOADING');
    const live = instances.filter(instance => instance.status === 'LIVE');
    const liveLeaders = instances.filter(instance => instance.status === 'LIVE' && instance.leader);
    const liveNotLeaders = instances.filter(instance => instance.status === 'LIVE' && !instance.leader);

    if (liveLeaders.length > 1) {
        return {status: 'ERROR', reason: 'MULTI', text: 'Multiple Leaders Running'};
    }
    else if (liveLeaders.length === 1 && live.length < 2) {
        return {status: 'LIVE', reason: 'NOBKP', text: 'No Running Backup'};
    }
    else if (liveLeaders.length === 1) {
        return {status: 'LIVE', reason: '-', text: '-'};
    }
    else if (loading.length > 0) {
        return {status: 'LOADING', reason: '-', text: '-'};
    }
    else if (liveNotLeaders.length > 0) {
        return {status: 'DOWN', reason: 'NO LEADER', text: 'No Elected Leader'};
    }
    else {
        return {status: 'DOWN', reason: 'DOWN', text: 'No Running Instances'};
    }
}

function computeStatusLoadBalancing(service: ServiceType, instances: Array<InstanceType>) {
    const loading = instances.filter(instance => instance.status === 'LOADING');
    const liveLeaders = instances.filter(instance => instance.status === 'LIVE' && instance.leader);

    const count = service.activeCount || 0;
    if (liveLeaders.length >= count) {
        return {status: 'LIVE', reason: '-', text: '-'};
    }
    else if (liveLeaders.length > 0 && loading.length > 0) {
        return {status: 'LIVE', reason: 'LOADING', text: '-'};
    }
    else if (liveLeaders.length > 0 && liveLeaders.length < count) {
        return {status: 'LIVE', reason: 'NOT ENOUGH', text: 'Not Enough Active Instances, Only ' + liveLeaders.length + " on " + count};
    }
    else {
        return {status: 'DOWN', reason: 'DOWN', text: 'No Active Instances, out of ' + count};
    }
}

function computeStatusOffline(service: ServiceType, instances: Array<InstanceType>) {
    const live = instances.filter(instance => instance.status === 'LIVE');

    if (live.length > 0) {
        return {status: 'LIVE', reason: 'PROCESSING', text: '-' };
    }
    else {
        return {status: 'OFFLINE', reason: '-', text: '-'};
    }
}

function computeServiceDetails(service: ServiceType, instances: Array<InstanceType>) {
    const unexpected = instances.filter(instance => instance.unexpected === true);
    const versions = new Set(instances.map(instance => instance.version));
    const confrevs = new Set(instances.map(instance => instance.confRevision));
    const confpend = instances.filter(instance => instance.confCommited === false);

    return {
        unexpected: unexpected.length > 0,
        versions: versions.size > 1,
        configs: confrevs.size > 1 || confpend.length > 0,
    }
}

export { Service, computeStatus }
export type { ServiceType, ServiceDetailType }
