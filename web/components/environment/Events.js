// @flow
import React from "react";
import {Badge} from "../common/Badge";

type EventType = {
    id: string,
    timestamp: string,
    group: 'RUNTIME'|'RELEASE'|'VERSION'|'INFO',
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
        return type === "version" ? "#58738f" :
               type === "release" ? "#5797ce" :
               type === "started" ? "#4ace51" :
               type === "online" ? "#4ace51" :
               type === "stopped" ? "#ce1b1c" :
               type === "offline" ? "#e39490" :
               type === "loading" ? "#e5ff5a" :
               type === "runtime" ? "#ffe603" :
               type === "info" ? "lightgray" :
               null
    }
    font(type: ?string) {
        return type === "version" ? "white" :
               type === "release" ? "white" :
               type === "started" ? null :
               type === "online" ? null :
               type === "stopped" ? "white" :
               type === "offline" ? "white" :
               type === "loading" ? null :
               type === "runtime" ? null :
               type === "info" ? null :
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
                            <Badge backgroundColor={this.color(event.type)} color={this.font(event.type)}
                                   style={{width: '50px', textAlign: 'center'}}>
                                {event.type}
                            </Badge>
                            &nbsp;
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