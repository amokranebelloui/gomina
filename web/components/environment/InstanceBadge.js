// @flow
import * as React from "react";
import type {InstanceType} from "./Instance";
import './InstanceBadge.css'
import {statusColor} from "./Status";

type Props = {
    instance: InstanceType
}

class InstanceBadge extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const color = statusColor(this.props.instance.status, this.props.instance.leader, this.props.instance.participating);
        return (
            <div className="instance-badge">
                <span style={{backgroundColor: color, color: "white"}}>{this.props.instance.status}</span>
                <span>{this.props.instance.name}</span>
                {this.props.instance.host &&
                    <span>{this.props.instance.host}</span>
                }
            </div>
        )
    }
}

export { InstanceBadge }
