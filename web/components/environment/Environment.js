// @flow
import * as React from "react";
import './Environment.css'
import type {ServiceDetailType} from "./Service";
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

type EnvDataType = {
    type?: ?string,
    description?: ?string,
    monitoringUrl?: ?string
}

type Props = {
    env: string,
    services: Array<ServiceDetailType>,
    highlight?: ?(InstanceType => boolean),
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

                    const highlightFunction = this.props.highlight || (instance => true);
                    var serviceHighlighted = false;
                    {svc.instances.map(instance =>
                        serviceHighlighted = serviceHighlighted || highlightFunction(instance)
                    )}
                    const opacity = serviceHighlighted ? 1 : 0.1;

                    const service = (
                        <tr key={'service' + svc.service.svc} className='env-row' style={{opacity: opacity}}>
                            <Route render={({ history}) => (
                            <ServiceStatus key={'status' + svc.service.svc}
                                           status={status.status} reason={status.reason} text={status.text}
                                           onClick={e => this.toggleService(svc.service.svc, history)} />
                            )} />
                            <Service service={svc.service} instances={svc.instances || []} />
                        </tr>
                    );
                    let instances = [];
                    if (this.isOpen(svc.service.svc)) {
                        instances = (svc.instances || []).map(instance => {
                            const highlighted = this.props.highlight != null && this.props.highlight(instance);
                            return (
                                <tr key={instance.id} className='env-row instance' style={{opacity: (highlighted) ? 1 : 0.06}}>
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

export {EnvironmentLogical}
export type {EnvType, EnvDataType}