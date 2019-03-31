// @flow
import * as React from "react"
import {ServiceModeEditor} from "./ServiceModeEditor";
import type {ServiceDataType, ServiceType} from "./Service";

type Props = {
    service: ServiceType,
    onChange?: (svc: string, data: ServiceDataType) => void
}

type State = {
    svc: string,
    type?: ?string,
    mode?: ?string,
    count?: ?string,
    componentId?: ?string
}

class ServiceEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            svc: this.props.service.svc,
            type: this.props.service.type,
            mode: this.props.service.mode,
            count: this.props.service.activeCount ? this.props.service.activeCount.toString() : "",
            componentId: this.props.service.componentId
        };
    }
    
    changeSvc(svc: string) {
        this.setState({svc: svc});
        this.notifyChange({svc: svc});
    }
    changeType(type: string) {
        this.setState({type: type});
        this.notifyChange({type: type});
    }
    changeMode(mode: ?string, count: ?string) {
        this.setState({mode: mode, count: count});
        this.notifyChange({mode: mode, count: count});
    }
    changeComponentId(componentId: string) {
        this.setState({componentId: componentId});
        this.notifyChange({componentId: componentId});
    }

    notifyChange(delta: any) {
        const fromState = {
            svc: this.state.svc,
            type: this.state.type,
            mode: this.state.mode,
            count: this.state.count ? parseInt(this.state.count) : null,
            componentId: this.state.componentId
        };
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(this.props.service.svc, data)
    }

    render() {
        return (
            <div>
                <input type="text" name="svc" placeholder="Service"
                       value={this.state.svc}
                       onChange={e => this.changeSvc(e.target.value)} />
                <br/>
                <input type="text" name="type" placeholder="Type"
                       value={this.state.type}
                       onChange={e => this.changeType(e.target.value)} />
                <br/>
                <ServiceModeEditor mode={this.state.mode}
                                   count={this.state.count}
                                   onChanged={(mode, count) => this.changeMode(mode, count)} />
                <br/>
                <input type="text" name="component" placeholder="Component"
                       value={this.state.componentId}
                       onChange={e => this.changeComponentId(e.target.value)} />
            </div>
        );
    }

}

export { ServiceEditor }