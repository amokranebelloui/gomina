// @flow
import * as React from "react"
import type {UserDataType, UserType} from "./UserType";
import {TagEditor} from "../common/TagEditor";

type Props = {
    user: UserType,
    onChange?: (userId: string, data: UserDataType) => void
}

type State = {
    login: string,
    shortName: string,
    firstName: ?string,
    lastName: ?string,
    accounts: Array<string>,
}

class UserEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            login: this.props.user && this.props.user.login,
            shortName: this.props.user && this.props.user.shortName,
            firstName: this.props.user && this.props.user.firstName,
            lastName: this.props.user && this.props.user.lastName,
            accounts: this.props.user && this.props.user.accounts || [],
        }
    }

    changeLogin(val: string) {
        this.setState({login: val});
        this.notifyChange({login: val});
    }
    changeShortName(val: string) {
        this.setState({shortName: val});
        this.notifyChange({shortName: val});
    }
    changeFirstName(val: ?string) {
        this.setState({firstName: val});
        this.notifyChange({firstName: val});
    }
    changeLastName(val: ?string) {
        this.setState({lastName: val});
        this.notifyChange({lastName: val});
    }
    addAccount(val: string) {
        const newAccounts = [...this.state.accounts];
        newAccounts.push(val);
        this.setState({accounts: newAccounts});
        this.notifyChange({accounts: newAccounts});
    }
    deleteAccount(val: string) {
        const newAccounts = [...this.state.accounts].filter(i => i !== val);
        this.setState({accounts: newAccounts});
        this.notifyChange({accounts: newAccounts});
    }

    notifyChange(delta: any) {
        const fromState = {
            login: this.state.login,
            shortName: this.state.shortName,
            firstName: this.state.firstName,
            lastName: this.state.lastName,
            accounts: this.state.accounts
        };
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(this.props.user.id, data)
    }

    render() {
        return (
            <div>
                <b>Login: </b>
                <input type="text" name="login" placeholder="Login"
                       value={this.state.login}
                       onChange={e => this.changeLogin(e.target.value)} />
                <br/>
                <b>Short Name: </b>
                <input type="text" name="shortName" placeholder="Short Name"
                       value={this.state.shortName}
                       onChange={e => this.changeShortName(e.target.value)} />
                <br/>
                <b>First Name: </b>
                <input type="text" name="firstName" placeholder="First Name"
                       value={this.state.firstName}
                       onChange={e => this.changeFirstName(e.target.value)} />
                <br/>
                <b>Last Name: </b>
                <input type="text" name="lastName" placeholder="last Name"
                       value={this.state.lastName}
                       onChange={e => this.changeLastName(e.target.value)} />
                <br/>
                <TagEditor tags={this.state.accounts} onTagAdd={a => this.addAccount(a)} onTagDelete={a => this.deleteAccount(a)} />
                <br/>

            </div>
        );
    }
}

export { UserEditor }