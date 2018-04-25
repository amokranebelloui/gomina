import React from "react";
import {Badge} from "../common/component-library";

function computeServiceDetails(serviceName, instances) {
    const versions = new Set(instances.map(instance => instance.version));
    const liveLeaders = instances.filter(instance => instance.status == 'LIVE' && instance.leader);
    const liveNotLeaders = instances.filter(instance => instance.status == 'LIVE' && !instance.leader);
    const loading = instances.filter(instance => instance.status == 'LOADING');
    const multiple = liveLeaders.length > 1;
    const noleader = liveLeaders.length == 0;
    console.log("-", versions, liveLeaders);

    let status;
    let reason;
    let text;
    if (liveLeaders.length > 1) {
        status = 'ERROR';
        reason = 'MULTI';
        text = 'Multiple instances running';
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
        status: status,
        reason: reason,
        text: text,
        versions: versions.size > 1,
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
        //const highlightFunction = this.props.highlightFunction || (instance => true);
        //const opacity = this.props.highlighted ? 1 : 0.06;
        /*
        {d.multiple && <Badge title="Multiple instances running" backgroundColor="orange">multi?</Badge>}
        {d.noleader && <Badge title="No leader" backgroundColor="orange">leader?</Badge>}
         */
        const opacity = 1;
        return ([
                <ServiceStatus key={'status' + serviceName}
                               status={d.status} reason={d.reason} text={d.text}
                               style={{opacity: opacity}} />,
                <div className="service">
                    <span><b>{serviceName}</b></span><br/>
                    {d.versions && <Badge title="Different versions between instances" backgroundColor="orange">versions?</Badge>}

                </div>
            ]
        )
    }
}

export { Service }