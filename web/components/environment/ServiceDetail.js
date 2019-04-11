// @flow
import * as React from "react"
import type {ServiceType} from "./Service";
import {TagCloud} from "../common/TagCloud";

type Props = {
    service: ServiceType
}

class ServiceDetail extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const service = this.props.service;
        return (
            <div>
                {service.svc}<br/>
                {service.type}<br/>
                <b>Mode: </b>{service.mode} {service.activeCount}<br/>
                <b>Component: </b>{service.component && service.component.label}<br/>
                <b>Systems: </b> <TagCloud tags={service.systems}/><br/>
            </div>
        );
    }
}

export { ServiceDetail }