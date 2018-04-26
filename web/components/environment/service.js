import React from "react";
import {Badge} from "../common/component-library";

function computeServiceDetails(serviceName, instances) {
    const unexpected = instances.filter(instance => instance.unexpected == true);
    const versions = new Set(instances.map(instance => instance.version));
    const confrevs = new Set(instances.map(instance => instance.confRevision));
    const confpend = instances.filter(instance => instance.confCommited == false);
    const liveLeaders = instances.filter(instance => instance.status == 'LIVE' && instance.leader);
    const live = instances.filter(instance => instance.status == 'LIVE');
    const liveNotLeaders = instances.filter(instance => instance.status == 'LIVE' && !instance.leader);
    const loading = instances.filter(instance => instance.status == 'LOADING');
    const multiple = liveLeaders.length > 1;
    const noleader = liveLeaders.length == 0;
    console.log("-", serviceName, confpend, confpend.length > 0);

    let status;
    let reason;
    let text;
    if (liveLeaders.length > 1) {
        status = 'ERROR';
        reason = 'MULTI';
        text = 'Multiple instances running';
    }
    else if (liveLeaders.length == 1 && live.length < 2) {
        status = 'LIVE';
        reason = 'NOBKP';
        text = 'No Running Backup';
    }
    else if (liveLeaders.length == 1) {
        status = 'LIVE';
        reason = '-';
        text = '-';
    }
    else if (loading.length > 0) {
        status = 'LOADING';
        reason = '-';
        text = '-';
    }
    else if (liveNotLeaders.length > 0) {
        status = 'DOWN';
        reason = 'NO LEADER';
        text = 'No elected Leader';
    }
    else {
        status = 'DOWN';
        reason = 'DOWN';
        text = 'No running instances';
    }
    console.info("$$ ", serviceName, status, liveLeaders, instances);
    return {
        unexpected: unexpected.length > 0,
        status: status,
        reason: reason,
        text: text,
        versions: versions.size > 1,
        configs: confrevs.size > 1 || confpend.length > 0,
        multiple: multiple,
        noleader: noleader,
    }
}

class ServiceStatus extends React.Component {
    render() {
        const status = this.props.status;
        const reason = this.props.reason;
        const text = this.props.text;
        const backgroundColor =
            status == 'ERROR' ? '#840513' :
            status == 'LIVE'  ? '#00ad0e' :
            status == 'LOADING' ? '#ffdc92' :
            status == 'DOWN'? '#FF0000' :
            'lightgray';
        const opacity = 0.9;
        return (
            <div className='status' style={Object.assign(this.props.style, {backgroundColor: backgroundColor, color: 'white'})}>
                <div style={{display: 'table', width: '100%', boxSizing: 'border-box', padding: '3px'}}>

                    <div style={{display: 'table-row'}}>
                        <div style={{display: 'table-cell', textAlign: 'left'}}>{status}</div>
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

class Service extends React.Component {
    render() {
        const serviceName = this.props.name;
        const instances = this.props.instances ? this.props.instances : [];
        const d = computeServiceDetails(serviceName, instances);
        const highlightFunction = this.props.highlightFunction || (instance => true);
        var serviceHighlighted = false;
        {instances.map(instance =>
            serviceHighlighted |= highlightFunction(instance)
        )}
        const opacity = serviceHighlighted ? 1 : 0.1;
        return ([
                <ServiceStatus key={'status' + serviceName}
                               status={d.status} reason={d.reason} text={d.text}
                               style={{opacity: opacity}} />,
                <div className="service" style={{opacity: opacity}}>
                    <span><b>{serviceName}</b></span><br/>
                    {d.unexpected && <Badge title="Unexpected instances running" backgroundColor="orange">unexpected?</Badge>}
                    {d.versions && <Badge title="Different versions between instances" backgroundColor="orange">versions?</Badge>}
                    {d.configs && <Badge title="Config not committed or different revisions between instances" backgroundColor="orange">conf?</Badge>}
                </div>
            ]
        )
    }
}

export { Service }