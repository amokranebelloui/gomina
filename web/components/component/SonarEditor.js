// @flow

import * as React from "react"

type Props = {
    servers: Array<string>,
    server?: ?string,
    onChanged?: (server: ?string) => void;
    onEdited?: (server: ?string) => void;
    onEditionCancelled?: () => void;
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
    changeServer(server?: ?string) {
        this.setState({server: server});
        this.props.onChanged && this.props.onChanged(server)
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
            <div style={{display: 'inline-block'}}>

                <select name="server"
                        value={this.state.server}
                        onChange={e => this.changeServer(e.target.value)}
                        onKeyPress={e => e.key === 'Enter' && this.update()}
                        onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                        style={{width: '50px', fontSize: 9}}>
                    <option value=""></option>
                    {this.props.servers && this.props.servers.map(s =>
                        <option value={s}>{s}</option>
                    )}
                </select>

                <input type="text" name="server" placeholder="Server" readOnly="true"
                       value={this.state.server}
                       onChange={e => this.changeServer(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       style={{width: '80px', fontSize: 9}}
                />
            </div>
        )
    }
}

export { SonarEditor }