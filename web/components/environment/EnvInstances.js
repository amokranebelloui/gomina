// @flow
import * as React from "react";
import './Environment.css'
import {Service} from "./Service";
import {Instance} from "./Instance";
import type {ServiceDetailType, ServiceType} from "./Service";
import type {InstanceType} from "./Instance";
import {Status} from "./Status";
import {flatMap, groupBy} from "../common/utils";
import Route from "react-router-dom/es/Route";

type Props = {
    services: Array<ServiceDetailType>,
    highlight?: ?(InstanceType => boolean),
}

class EnvInstances extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    navigate(instance: InstanceType, history: any) {
        history.push("/envs/" + instance.env + "/" + instance.id)
    }
    render() {
        const services = this.props.services;

        const instances = flatMap(services, s => s.instances);
        const byHost = {};

        instances.forEach(instance => {
            if (instance.host !== null) {
                if (byHost[instance.host] == null) byHost[instance.host] = [];
                byHost[instance.host].push(instance)
            }
            else if (instance.deployHost !== null) {
                if (byHost[instance.deployHost] == null) byHost[instance.deployHost] = [];
                byHost[instance.deployHost].push(instance)
            }
            else {
                const key = "unknown";
                if (byHost[key] == null) byHost[key] = [];
                byHost[key].push(instance)
            }
        });
        
        const hosts = [...new Set(Object.keys(byHost))].sort();

        const instancesFor = function (h) {
            return byHost[h] || []
        };

        return (
            <div>
                {hosts.map( h =>
                    <div>
                        <span>{h != "null" ? h : '***'}&nbsp;</span>
                        <table className='env-table'>
                            <tbody>
                            {(instancesFor(h)).map(instance =>
                                <tr key={instance.id} className='env-row instance'>

                                    <Route render={({ history}) => (
                                        <Status key={'status' + instance.id} status={instance.status} leader={instance.leader}
                                                participating={instance.participating} cluster={instance.cluster}
                                                style={{opacity: (this.props.highlight != null && this.props.highlight(instance)) ? 1 : 0.06}}
                                                onClick={e => this.navigate(instance, history)} />
                                    )} />
                                    <Instance key={'instance' + instance.id} instance={instance} highlighted={this.props.highlight != null && this.props.highlight(instance)} />
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

        )
    }
}

export {EnvInstances}
