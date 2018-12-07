// @flow
import * as React from "react";
import './Environment.css'
import {Service} from "./Service";
import {Instance} from "./Instance";
import type {ServiceType} from "./Service";
import type {InstanceType} from "./Instance";

type ServiceDetailType = {
    service: ServiceType,
    instances: Array<InstanceType>
}

type Props = {
    services: Array<ServiceDetailType>,
    highlight?: ?(InstanceType => boolean),
}

class EnvironmentLogical extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const services = this.props.services;
        return (
            <div className='env-table'>
                {services.map( svc =>
                    <div key={svc.service.svc} className='env-row'>
                        <Service service={svc.service} instances={svc.instances || []} highlightFunction={this.props.highlight} />
                        {(svc.instances || []).map(instance =>
                            <Instance key={instance.id} instance={instance} highlighted={this.props.highlight != null && this.props.highlight(instance)} />
                        )}
                    </div>
                )}
            </div>
        )
    }
}

export {EnvironmentLogical}
