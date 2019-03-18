// @flow

import * as React from "react"

type Props = {
    server?: ?string,
    onEdited: (server: ?string) => void;
    onEditionCancelled: () => void;
}

type State = {
    server?: ?string
}

class SonarEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            server: this.props.server
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    update() {
        this.props.onEdited && this.props.onEdited(this.state.server)
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
            <div>
                <input type="text" name="server" placeholder="Server"
                       value={this.state.server}
                       onChange={e => this.setState({server: e.target.value})}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       style={{width: '80px', fontSize: 9}}
                />
            </div>
        )
    }
}

export { SonarEditor }