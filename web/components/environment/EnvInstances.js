// @flow
import * as React from "react";
import './Environment.css'
import {Service} from "./Service";
import {Instance} from "./Instance";
import type {ServiceDetailType, ServiceType} from "./Service";
import type {InstanceType} from "./Instance";
import {Status} from "./Status";
import {flatMap} from "../common/utils";

type Props = {
    services: Array<ServiceDetailType>,
    highlight?: ?(InstanceType => boolean),
}

class EnvInstances extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const services = this.props.services;

        const instances = flatMap(services, s => s.instances);

        return (
            <table className='env-table'>
                <tbody>
                {(instances || []).map(instance =>
                    <tr key={instance.id} className='env-row instance'>
                        <Status key={'status' + instance.id} status={instance.status} leader={instance.leader}
                                participating={instance.participating} cluster={instance.cluster}
                                style={{opacity: (this.props.highlight != null && this.props.highlight(instance)) ? 1 : 0.06}}
                        />
                        <Instance key={'instance' + instance.id} instance={instance} highlighted={this.props.highlight != null && this.props.highlight(instance)} />
                    </tr>
                )}
                </tbody>
            </table>
        )
    }
}

export {EnvInstances}
