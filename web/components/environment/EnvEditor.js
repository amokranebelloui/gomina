// @flow
import * as React from "react"
import type {EnvDataType, EnvType} from "./Environment";

type Props = {
    env: EnvType,
    onChange?: (id: string, data: EnvDataType) => void
}

type State = {
    id?: ?string,
    type?: ?string,
    description?: ?string,
    monitoringUrl?: ?string
}

class EnvEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            id: this.props.env.env,
            type: this.props.env.type,
            description: this.props.env.description,
            monitoringUrl: this.props.env.monitoringUrl
        };
    }
    changeType(type: string) {
        this.setState({type: type});
        this.notifyChange({type: type})
    }
    changeDescription(description: string) {
        this.setState({description: description});
        this.notifyChange({description: description})
    }
    changeMonitoringUrl(monitoringUrl: string) {
        this.setState({monitoringUrl: monitoringUrl});
        this.notifyChange({monitoringUrl: monitoringUrl})
    }

    notifyChange(delta: any) {
        const fromState = {
            type: this.state.type,
            description: this.state.description,
            monitoringUrl: this.state.monitoringUrl
        };
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(this.props.env.env, data)
    }

    render() {
        return (
            <div>
                <input type="text" name="id" placeholder="env Id"
                       value={this.state.id} readOnly={true} />
                <br/>
                <input type="text" name="type" placeholder="Type"
                       value={this.state.type}
                       onChange={e => this.changeType(e.target.value)} />
                <br/>
                <input type="text" name="description" placeholder="Description"
                       value={this.state.description}
                       onChange={e => this.changeDescription(e.target.value)} />
                <br/>
                <input type="text" name="monitoringUrl" placeholder="Monitoring URL"
                       value={this.state.monitoringUrl}
                       style={{width: '200px'}}
                       onChange={e => this.changeMonitoringUrl(e.target.value)} />
            </div>
        );
    }
}

export { EnvEditor }