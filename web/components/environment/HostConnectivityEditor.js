// @flow
import * as React from "react"
import type {HostType} from "./HostType";
import type {HostConnectivityDataType} from "./HostType";

type Props = {
    host: HostType,
    onChange?: (hostId: string, data: HostConnectivityDataType) => void
}

type State = {
    username?: ?string,
    passwordAlias?: ?string,
    proxyUser?: ?string,
    proxyHost?: ?string,
    sudo?: ?string
}

class HostConnectivityEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            username: this.props.host.username,
            passwordAlias: this.props.host.passwordAlias,
            proxyHost: this.props.host.proxyHost,
            proxyUser: this.props.host.proxyUser,
            sudo: this.props.host.sudo,
        };
    }
    changeUsername(value: string) {
        this.setState({username: value});
        this.notifyChange({username: value});
    }
    changePasswordAlias(value: string) {
        this.setState({passwordAlias: value});
        this.notifyChange({passwordAlias: value});
    }
    changeProxyHost(val: string) {
        this.setState({proxyHost: val});
        this.notifyChange({proxyHost: val});
    }
    changeProxyUser(val: ?string) {
        this.setState({proxyUser: val});
        this.notifyChange({proxyUser: val});
    }
    changeSudo(val: ?string) {
        this.setState({sudo: val});
        this.notifyChange({sudo: val});
    }

    notifyChange(delta: any) {
        const fromState = {
            username: this.state.username,
            passwordAlias: this.state.passwordAlias,
            proxyHost: this.state.proxyHost,
            proxyUser: this.state.proxyUser,
            sudo: this.state.sudo
        };
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(this.props.host.host, data)
    }
    render() {
        return (
            <div>
                <input type="text" name="username" placeholder="Username"
                       value={this.state.username}
                       onChange={e => this.changeUsername(e.target.value)} />
                <br/>
                <input type="text" name="passwordAlias" placeholder="PasswordAlias"
                       value={this.state.passwordAlias}
                       onChange={e => this.changePasswordAlias(e.target.value)} />
                <br/>
                <input type="text" name="proxyHost" placeholder="proxyHost"
                       value={this.state.proxyHost}
                       onChange={e => this.changeProxyHost(e.target.value)} />
                <br/>
                <input type="text" name="proxyUser" placeholder="proxyUser"
                       value={this.state.proxyUser}
                       onChange={e => this.changeProxyUser(e.target.value)} />
                <br/>
                <input type="text" name="sudo" placeholder="sudo"
                       value={this.state.sudo}
                       onChange={e => this.changeSudo(e.target.value)} />
                <br/>

            </div>
        );
    }
}

export { HostConnectivityEditor }