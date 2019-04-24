// @flow
import * as React from "react"
import {ServiceModeEditor} from "./ServiceModeEditor";
import type {ServiceDataType, ServiceType} from "./Service";
import {Autocomplete} from "../common/AutoComplete";
import type {ComponentRefType} from "../component/ComponentType";

type Props = {
    service: ServiceType,
    components: Array<ComponentRefType>,
    onChange?: (svc: string, data: ServiceDataType) => void
}

type State = {
    svc?: string,
    type?: ?string,
    mode?: ?string,
    activeCount?: ?string,
    component?: ?ComponentRefType
}

class ServiceEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            svc: this.props.service.svc,
            type: this.props.service.type,
            mode: this.props.service.mode,
            activeCount: this.props.service.activeCount ? this.props.service.activeCount.toString() : "",
            component: this.props.service.component
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
        this.setState({mode: mode, activeCount: count});
        this.notifyChange({mode: mode, activeCount: count});
    }
    changeComponentId(component: ?ComponentRefType) {
        this.setState({component: component});
        this.notifyChange({componentId: component && component.id});
    }

    notifyChange(delta: any) {
        const fromState = {
            svc: this.state.svc,
            type: this.state.type,
            mode: this.state.mode,
            activeCount: this.state.activeCount ? parseInt(this.state.activeCount) : null,
            componentId: this.state.component && this.state.component.id
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
                                   count={this.state.activeCount}
                                   onChanged={(mode, count) => this.changeMode(mode, count)} />
                <br/>
                <Autocomplete value={this.state.component}
                              suggestions={this.props.components}
                              idGetter={s => s.id} labelGetter={s => s.label}
                              onChange={c => this.changeComponentId(c)} />

            </div>
        );
    }

}

export { ServiceEditor }