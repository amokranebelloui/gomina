// @flow
import * as React from "react";
import './Environment.css'
import type {ServiceDetailType, ServiceType} from "./Service";
import {computeStatus, Service} from "./Service";
import type {InstanceType} from "./Instance";
import {Instance} from "./Instance";
import {Status} from "./Status";
import {ServiceStatus} from "./ServiceStatus";
import Route from "react-router-dom/es/Route";

type EnvType = {
    env: string,
    type?: ?string,
    description?: ?string,
    monitoringUrl?: ?string,
    active: boolean
}

type EnvHostsType = {
    env: string,
    type?: ?string,
    description?: ?string,
    active: boolean,
    hosts: Array<string>
}

type EnvDataType = {
    type?: ?string,
    description?: ?string,
    monitoringUrl?: ?string
}

type Props = {
    env: string,
    services: Array<ServiceDetailType>,
    highlight?: ?((?InstanceType, ?ServiceType) => boolean),
    onOrderChange?: (service: string, targetService: string) => void
}

type State = {
    open: any
}

class EnvironmentLogical extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            open: {}
        }
    }
    toggleService(service: string, history: any) {
        this.navigateService(service, history);
        this.isOpen(service)
            ? this.closeService(service)
            : this.openService(service)
    }
    openService(service: string) {
        //console.info("OPENED", path);
        const newOpen = Object.assign({}, this.state.open);
        newOpen[service] = true;
        this.setState({open: newOpen})
    }
    closeService(service: string) {
        //console.info("CLOSED", path);
        const newOpen = Object.assign({}, this.state.open);
        newOpen[service] = false;
        this.setState({open: newOpen})
    }
    isOpen(service: string) {
        //console.info('is open', path, this.props.openPaths[path], this.props.openPaths[path] == true);
        return this.state.open[service] == true
    }
    navigate(svc: string, instance: InstanceType, history: any) {
        history.push("/envs/" + instance.env + "/svc/" + svc + "/" + instance.id)
    }
    navigateService(svc: string, history: any) {
        history.push("/envs/" + this.props.env + "/svc/" + svc)
    }
    render() {
        const services = this.props.services;

        return (
            <table className='env-table'>
                <tbody>
                {services.map( svc => {
                    const status = computeStatus(svc.service, svc.instances);

                    const highlightFunction = this.props.highlight || ((instance, svc) => true);
                    let serviceHighlighted = highlightFunction(null, svc.service);
                    if (svc.instances && svc.instances.length > 0) {
                        //serviceHighlighted = false;
                        {svc.instances.map(instance =>
                            serviceHighlighted = serviceHighlighted || highlightFunction(instance, svc.service)
                        )}
                    }
                    else if (!this.props.highlight) {
                        serviceHighlighted = true
                    }
                    const opacity = serviceHighlighted ? 1 : 0.2;

                    const service = (
                        <tr key={'service' + svc.service.svc} className='env-row' style={{opacity: opacity}}>
                            <Route render={({ history}) => (
                            <ServiceStatus key={'status' + svc.service.svc}
                                           service={svc.service.svc}
                                           status={status.status} reason={status.reason} text={status.text}
                                           onClick={e => this.toggleService(svc.service.svc, history)}
                                           onOrderChange={(s, t) => this.props.onOrderChange && this.props.onOrderChange(s, t)}
                            />
                            )} />
                            <Service service={svc.service} instances={svc.instances || []} />
                        </tr>
                    );
                    let instances = [];
                    if (this.isOpen(svc.service.svc)) {
                        instances = sortInstances(svc.instances || []).map(instance => {
                            const highlightFunction = this.props.highlight || ((instance, svc) => true);
                            const highlighted = highlightFunction(instance, svc.service);
                            return (
                                <tr key={instance.id} className='env-row instance' style={{opacity: (highlighted) ? 1 : 0.1}}>
                                    <td key={'space' + instance.id} style={{display: 'table-cell', minWidth: '30px'}}></td>
                                    <Route render={({ history}) => (
                                        <Status key={'status' + instance.id} status={instance.status} leader={instance.leader}
                                                participating={instance.participating} cluster={instance.cluster}
                                                onClick={e => this.navigate(svc.service.svc, instance, history)}
                                        />
                                    )} />
                                    <Instance key={'instance' + instance.id} instance={instance} />
                                </tr>
                            )});
                    }
                    return [service].concat(instances)
                })
                }
                </tbody>
            </table>
        )
    }
}

function sortInstances(instances: Array<InstanceType>) {
    return instances.sort((a, b) => a.id > b.id ? 1 : -1)
}

export {EnvironmentLogical}
export type {EnvType, EnvDataType, EnvHostsType}