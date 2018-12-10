// @flow
import * as React from "react";
import './Environment.css'
import {Service} from "./Service";
import {Instance} from "./Instance";
import type {ServiceType} from "./Service";
import type {InstanceType} from "./Instance";
import {Status} from "./Status";

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
            <table className='env-table'>
                <tbody>
                {services.map( svc => [
                    [
                        <tr key={'service' + svc.service.svc} className='env-row'>
                            <Service service={svc.service} instances={svc.instances || []} highlightFunction={this.props.highlight} />
                        </tr>
                    ].concat(
                        (svc.instances || []).map(instance =>
                            <tr key={instance.id} className='env-row'>
                                <td key={'space' + instance.id} style={{display: 'table-cell', minWidth: '30px'}}></td>
                                <Status key={'status' + instance.id} status={instance.status} leader={instance.leader}
                                        participating={instance.participating} cluster={instance.cluster}
                                        style={{opacity: (this.props.highlight != null && this.props.highlight(instance)) ? 1 : 0.06}}
                                />
                                <Instance key={'instance' + instance.id} instance={instance} highlighted={this.props.highlight != null && this.props.highlight(instance)} />
                            </tr>
                        )
                    )]
                )}
                </tbody>
            </table>
        )
    }
}

export {EnvironmentLogical}
