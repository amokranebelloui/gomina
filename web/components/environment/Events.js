// @flow
import React from "react";
import {Badge} from "../common/Badge";

type EventType = {
    id: string,
    timestamp: string,
    type?: ?string,
    message?: ?string,
    envId?: ?string,
    instanceId?: ?string,
    componentId?: ?string,
    version?: ?string
}

type Props = {
    events: Array<EventType>,
    errors: Array<string>
}

class Events extends React.Component<Props> {
    constructor(props: Props) {
        super(props)
    }
    color(type: ?string) {
        return type === "release" ? "#5797ce" :
               type === "runtime" ? "#90ce8d" :
               type === "info" ? "lightgray" :
               null
    }
    render() {
        return (
            <div>
                {(this.props.errors||[]).map(error =>
                    <li key={error} style={{fontStyle: 'italic', color: 'gray'}}>
                        {error}
                    </li>
                )}
                <ul>
                    {this.props.events && this.props.events.map(event =>
                        <li key={event.id} title={event.id} style={{margin: "1px"}}>
                            {new Date(event.timestamp).toLocaleString()}&nbsp;
                            <Badge backgroundColor={this.color(event.type)}>{event.type}</Badge>&nbsp;
                            {event.message}&nbsp;
                            <span style={{ color: 'lightgray', ontStyle: 'italic'}}>{event.envId} {event.instanceId}</span>
                            &nbsp;
                            <span style={{ color: 'lightgray', ontStyle: 'italic'}}>{event.componentId} {event.version}</span>
                            &nbsp;
                        </li>
                    )}
                </ul>
            </div>
        )
    }
}

export { Events }
export type { EventType }