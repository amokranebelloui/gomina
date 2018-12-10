// @flow
import * as React from "react";
import './Environment.css'
import type {ServiceDetailType} from "./Service";
import {Service} from "./Service";
import type {InstanceType} from "./Instance";

type Props = {
    services: Array<ServiceDetailType>,
    highlight?: ?(InstanceType => boolean),
}

class EnvServices extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const services = this.props.services;
        return (
            <table className='env-table' border="1">
                <tbody>
                {services.map( svc =>
                    <tr key={'service' + svc.service.svc} className='env-row'>
                        <Service service={svc.service} instances={svc.instances || []} highlightFunction={this.props.highlight} />
                    </tr>
                )}
                </tbody>
            </table>
        )
    }
}

export {EnvServices}
