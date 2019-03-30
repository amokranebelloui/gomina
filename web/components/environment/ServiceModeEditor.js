// @flow
import * as React from "react"

type Props = {
    mode?: ?string,
    count?: ?string,
    onChanged?: (mode: ?string, count: ?string) => void;
    onEdited?: (mode: ?string, count: ?string) => void;
    onEditionCancelled?: () => void;
}

type State = {
    mode?: ?string,
    count?: ?string
}

class ServiceModeEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            mode: this.props.mode,
            count: this.props.count
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    changeMode(mode: ?string) {
        this.setState({mode: mode});
        this.props.onChanged && this.props.onChanged(mode, this.state.count)
    }
    changeCount(count: ?string) {
        this.setState({count: count});
        this.props.onChanged && this.props.onChanged(this.state.mode, count)
    }
    update() {
        this.props.onEdited && this.props.onEdited(this.state.mode, this.state.count)
    }
    cancelEdition() {
        this.props.onEditionCancelled && this.props.onEditionCancelled()
    }
    componentDidMount() {
        //$FlowFixMe
        setTimeout(() => this.textInput.current && this.textInput.current.focus(), 0)
    }
    render() {
        return (
            <div style={{display: 'inline-block'}}>
                <select name="mode" value={this.state.mode}
                        onChange={e => this.changeMode(e.target.value)}
                        style={{width: '110px', fontSize: 9}}>
                    <option value=""></option>
                    <option value="ONE_ONLY">ONE_ONLY</option>
                    <option value="LEADERSHIP">LEADERSHIP</option>
                    <option value="LOAD_BALANCING">LOAD_BALANCING</option>
                    <option value="OFFLINE">OFFLINE</option>
                </select>

                <input type="text" name="count" placeholder="Count"
                       value={this.state.count}
                       onChange={e => this.changeCount(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       style={{width: '20px', fontSize: 9}}
                />
            </div>
        );
    }
}

export { ServiceModeEditor }