import React from "react";
import {Badge} from "../common/component-library";
import {BuildLink} from "../project/project";

class Service extends React.Component {
    render() {
        const service = this.props.service;
        const instances = this.props.instances ? this.props.instances : [];
        const projectSet = new Set(instances.map(instance => instance.project).filter(p => p != null));
        const projects = [...projectSet];
        const status = computeStatus(service, instances);
        const d = computeServiceDetails(service, instances);
        const highlightFunction = this.props.highlightFunction || (instance => true);
        var serviceHighlighted = false;
        {instances.map(instance =>
            serviceHighlighted |= highlightFunction(instance)
        )}
        const opacity = serviceHighlighted ? 1 : 0.1;
        return ([
                <ServiceStatus key={'status' + service.svc}
                               status={status.status} reason={status.reason} text={status.text}
                               style={{opacity: opacity}} />,
                <div className="service" style={{opacity: opacity}}>
                    <span><b>{service.svc}</b>&nbsp;<i>{service.type}</i></span>&nbsp;
                    <Badge backgroundColor="">{instances.length}</Badge><br/>
                    {service.mode}
                    {projects.map(project =>
                        <BuildLink url={'navigate/' + project}/>
                    )}
                    {d.unexpected && <Badge title="Unexpected instances running" backgroundColor="orange">exp?</Badge>}
                    {d.versions && <Badge title="Different versions between instances" backgroundColor="orange">versions?</Badge>}
                    {d.configs && <Badge title="Config not committed or different revisions between instances" backgroundColor="orange">conf?</Badge>}
                </div>
            ]
        )
    }
}

class ServiceStatus extends React.Component {
    render() {
        const status = this.props.status;
        const reason = this.props.reason;
        const text = this.props.text;
        const backgroundColor =
            status == 'ERROR' ? '#840513' :
                status == 'LIVE' && reason !== '-' ? '#6ebf75' :
                    status == 'LIVE'  ? '#00ad0e' :
                        status == 'LOADING' ? '#ffdc92' :
                            status == 'DOWN'? '#FF0000' :
                                'lightgray';
        const opacity = 0.9;
        return (
            <div className='status' style={Object.assign(this.props.style, {backgroundColor: backgroundColor, color: 'white', cursor: 'pointer'})}>
                <div style={{display: 'table', width: '100%', boxSizing: 'border-box', padding: '3px'}}>

                    <div style={{display: 'table-row'}}>
                        <div style={{display: 'table-cell', textAlign: 'left', fontSize: '15px'}}>{status}</div>
                    </div>
                    <div style={{display: 'table-row'}}>
                        <div style={{display: 'table-cell', textAlign: 'right'}}>
                            <span title={text} style={{fontSize: '7px', opacity: opacity}}>
                                {reason}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

function computeStatus(service, instances) {
    switch (service.mode) {
        case "ONE_ONLY": return computeStatusOnlyOne(service, instances);
        case "LEADERSHIP": return computeStatusLeadership(service, instances);
        case "LOAD_BALANCING": return computeStatusLoadBalancing(service, instances);
        case "OFFLINE": return computeStatusOffline(service, instances);
    }
    return {
        status: 'UNKNOWN',
        reason: 'UNKNOWN',
        text: 'Unknown mode ' + service.mode,
    }
}

function computeStatusOnlyOne(service, instances) {
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

function computeStatusLeadership(service, instances) {
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

function computeStatusLoadBalancing(service, instances) {
    const loading = instances.filter(instance => instance.status === 'LOADING');
    const liveLeaders = instances.filter(instance => instance.status === 'LIVE' && instance.leader);

    const count = service.activeCount;
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

function computeStatusOffline(service, instances) {
    const live = instances.filter(instance => instance.status === 'LIVE');

    if (live.length > 0) {
        return {status: 'LIVE', reason: 'PROCESSING', text: '-' };
    }
    else {
        return {status: 'OFFLINE', reason: '-', text: '-'};
    }
}

function computeServiceDetails(service, instances) {
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

export { Service }