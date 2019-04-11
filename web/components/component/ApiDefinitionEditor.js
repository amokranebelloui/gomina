// @flow
import * as React from "react"
import type {ApiDefinitionType, FunctionType} from "./ApiType";

type Props = {
    type: ApiDefinitionType,
    api: FunctionType,
    onChange?: (data: FunctionType) => void,
    onAdd?: (data: FunctionType) => void,
    onCancel?: () => void
}

type State = FunctionType


class ApiDefinitionEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            name: this.props.api.name,
            type: this.props.api.type,
            usage: this.props.api.usage,
            sources: []
        }
    }

    changeName(val: string) {
        this.setState({name: val});
        this.notifyChange({name: val});
    }
    changeType(val: string) {
        this.setState({type: val});
        this.notifyChange({type: val});
    }
    changeUsage(val: string) {
        this.setState({usage: val});
        this.notifyChange({usage: val});
    }
    notifyChange(delta: any) {
        const fromState = this.state;
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(data)
    }
    add() {
        this.props.onAdd && this.props.onAdd(this.state);
    }
    cancel() {
        this.setState({
            name: "",
            type: "",
            usage: "",
            sources: []
        });
        this.props.onCancel && this.props.onCancel();
    }
    componentDidMount() {
        console.info("api editor mount")
    }
    componentWillReceiveProps(nextProps: Props) {
        console.info("api editor props", nextProps);
        this.state = {
            name: this.props.api.name,
            type: this.props.api.type,
            usage: this.props.api.usage,
            sources: []
        }
    }
    render() {
        return (
            <div>
                <input type="text" name="name" placeholder="Name"
                       value={this.state.name}
                       onChange={e => this.changeName(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.add()}
                       onKeyDown={e => e.key === 'Escape' && this.cancel()}
                       style={{width: 120}}
                />
                <input type="text" name="type" placeholder="Type"
                       value={this.state.type}
                       onChange={e => this.changeType(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.add()}
                       onKeyDown={e => e.key === 'Escape' && this.cancel()}
                       style={{width: 70}}
                />
                {this.props.type === 'usage' &&
                    <input type="text" name="usage" placeholder="Usage"
                           value={this.state.usage}
                           onChange={e => this.changeUsage(e.target.value)}
                           onKeyPress={e => e.key === 'Enter' && this.add()}
                           onKeyDown={e => e.key === 'Escape' && this.cancel()}
                           style={{width: 70}}
                    />
                }
                <button onClick={() => this.add()}>Add</button>
                <button onClick={() => this.cancel()}>Cancel</button>
            </div>
        );
    }
}

export { ApiDefinitionEditor }
