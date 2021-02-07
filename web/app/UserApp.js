// @flow
import React, {Fragment} from "react"
import {Secure} from "../components/permission/Secure"
import axios from "axios/index";
import Link from "react-router-dom/es/Link";
import type {KnowledgeType} from "../components/knowledge/Knowledge";
import {Knowledge} from "../components/knowledge/Knowledge";
import {Badge} from "../components/common/Badge";
import type {UserDataType, UserType} from "../components/user/UserType";
import {InlineAdd} from "../components/common/InlineAdd";
import Route from "react-router-dom/es/Route";
import {UserEditor} from "../components/user/UserEditor";
import {ApplicationLayout} from "./common/ApplicationLayout";


type Props = {
    match: Object // FIXME Type react match object
};
type State = {
    user: ?UserType,
    knowledge: Array<KnowledgeType>,
    users: Array<UserType>,
    passwordEdition: boolean,
    newPassword: ?string,
    passwordEditionError: ?string,

    newUserData: ?UserDataType,
    userEdition: boolean,
    userEdited: ?UserDataType
};

class UserApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            user: null,
            knowledge: [],
            users: [],
            passwordEdition: false,
            newPassword: null,
            passwordEditionError: null,

            newUserData: {},
            userEdition: false,
            userEdited: null
        }
    }
    retrieveUsers() {
        console.log("userApp Retr Users ... ");
        const thisComponent = this;
        axios.get('/data/user')
            .then(response => {
                console.log("userApp users", response.data);
                thisComponent.setState({users: response.data});
            })
            .catch(function (error) {
                console.log("userApp users error", error);
                thisComponent.setState({users: []});
            });
    }
    retrieveUser(userId: string) {
        console.log("userApp Retr User ... ");
        const thisComponent = this;
        axios.get('/data/user/' + userId)
            .then(response => {
                console.log("userApp user user", response.data);
                document.title = response.data.shortName + ' - User';
                thisComponent.setState({user: response.data});
            })
            .catch(function (error) {
                console.log("userApp error", error);
                thisComponent.setState({user: null});
            });
    }
    retrieveKnowledge(userId: string) {
        const thisComponent = this;
        axios.get('/data/knowledge/user/' + userId)
            .then(response => thisComponent.setState({knowledge: response.data}))
            .catch(() => thisComponent.setState({knowledge: []}))
    }
    clearNewUserState() {
        this.setState({
            newUserData: null
        })
    }
    addUser() {
        return axios.post('/data/user/add', this.state.newUserData);
    };
    onUserAdded(userId: string, history: any) {
        this.retrieveUsers();
        history.push("/user/" + userId)
    };
    editUser() {
        this.setState({"userEdition": true});
    }
    changeUser(work: UserDataType) {
        console.info("User Changed", work);
        this.setState({"userEdited": work});
    }
    cancelUserEdition() {
        this.setState({"userEdition": false});
    }
    updateUser() {
        if (this.state.user) {
            const thisComponent = this;
            const userId = this.state.user.id;
            userId && axios.put('/data/user/' + userId + '/update', this.state.userEdited)
                .then(() => {
                    thisComponent.retrieveUsers();
                    thisComponent.retrieveUser(userId);
                })
                .catch((error) => console.log("User update error", error.response));
            this.setState({"userEdition": false}); // FIXME Display Results/Errors
        }
    }

    enable() {
        this.state.user && axios.put('/data/user/' + this.state.user.id + '/enable')
            .then(() => this.state.user && this.retrieveUser(this.state.user.id))
    }
    disable() {
        this.state.user && axios.put('/data/user/' + this.state.user.id + '/disable')
            .then(() => this.state.user && this.retrieveUser(this.state.user.id))
    }
    resetPassword() {
        this.state.user && axios.put('/data/user/' + this.state.user.id + '/reset-password');
    }
    changePassword() {
        const thisComponent = this;
        this.state.user && this.state.newPassword &&
            axios.put('/data/user/' + this.state.user.id + '/change-password?password=' + this.state.newPassword)
                .then(() => thisComponent.setState({passwordEdition: false}))
                .catch((error) => thisComponent.setState({passwordEditionError: "Cannot change password at the moment"}));
    }
    componentDidMount() {
        document.title = 'Users';
        console.info("userApp !did mount ");
        this.retrieveUsers();
        this.props.match.params.id && this.retrieveUser(this.props.match.params.id);
        this.props.match.params.id && this.retrieveKnowledge(this.props.match.params.id)
    }
    componentWillReceiveProps(nextProps: Props) {
        const newUserId = nextProps.match.params.id;
        console.info("userApp !did willRecProps ", newUserId, nextProps);
        this.retrieveUsers();
        if (this.props.match.params.id != newUserId) {
            if (newUserId) {
                this.retrieveUser(newUserId);
                this.retrieveKnowledge(newUserId);
            }
            else {
                this.setState({user: null});
            }
        }
    }

    render() {

        return (
            <ApplicationLayout title="Users"
                main={() =>
                    <Fragment>
                        {!!this.state.user &&
                        <div>
                            {!this.state.userEdition &&
                            <div>
                                <div style={{float: 'right'}} className='items'>
                                    <Secure permission="user.manage">
                                        <Secure key="Edit" permission="user.manage">
                                            <button onClick={() => this.editUser()}>Edit</button>
                                        </Secure>
                                        {this.state.user.disabled && <button onClick={e => this.enable()}>Enable</button>}
                                        {!this.state.user.disabled && <button onClick={e => this.disable()}>Disable</button>}
                                        <button onClick={e => this.resetPassword()}>Reset Password</button>
                                    </Secure>
                                </div>
                                <b>Id: </b>{this.state.user.id}
                                <br/>
                                <b>Login: </b>
                                {this.state.user.login}
                                {this.state.user.disabled &&
                                    <span style={{textDecoration: 'line-through', marginLeft: '5px'}}>DISABLED</span>
                                }
                                <br/>
                                <b>Short Name: </b>
                                {this.state.user.shortName}
                                <br/>
                                <b>Full Name: </b>
                                {this.state.user.firstName} {this.state.user.lastName}
                                <br/>
                                <b>Accounts: </b>
                                <span className="items">
                                {this.state.user.accounts.map(account =>
                                    <Badge key={account} backgroundColor={'#EEEEEE'}>{account}</Badge>
                                )}
                                </span>
                                <br/>
                                <b>Knowledge</b>
                                <Knowledge knowledge={this.state.knowledge} hideUser={true} />
                                <br/>
                            </div>
                            }

                            {this.state.userEdition && 
                            <div>
                                <UserEditor user={this.state.user}
                                            onChange={(id, u) => this.changeUser(u)} />
                                <hr/>
                                <button onClick={() => this.updateUser()}>Update</button>
                                <button onClick={() => this.cancelUserEdition()}>Cancel</button>
                            </div>
                            }

                            <hr/>

                            <br/>
                            <Secure condition={(user) => !!this.state.user && user === this.state.user.id}>
                                {!this.state.passwordEdition &&
                                    <button onClick={e => this.setState({passwordEdition: true, newPassword: ''})}>Change Password</button>
                                }
                                {this.state.passwordEdition &&
                                    <Fragment>
                                        <b>Password: </b>
                                        <input name="password" type="password" value={this.state.newPassword}
                                               onChange={e => this.setState({newPassword: e.target.value})}/>
                                        <button onClick={e => this.changePassword()}>Change</button>
                                        <button onClick={e => this.setState({passwordEdition: false})}>Cancel</button>
                                        {this.state.passwordEditionError && <i style={{color: 'red'}}>{this.state.passwordEditionError}</i>}
                                    </Fragment>
                                }
                                <br/>

                            </Secure>
                        </div>
                        }
                    </Fragment>
                }
                sidePrimary={() =>
                    <Secure permission="user.add">
                        <Route render={({ history }) => (
                            <InlineAdd type="User"
                                       action={() => this.addUser()}
                                       onEnterAdd={() => this.clearNewUserState()}
                                       onItemAdded={(work) => this.onUserAdded(work, history)}
                                       editionForm={() =>
                                           <UserEditor user={{}}
                                                       onChange={(id, u) => this.setState({newUserData: u})} />
                                       }
                                       successDisplay={(data: any) =>
                                           <div>
                                               <b>{data ? ("Added " + JSON.stringify(data)) : 'no data returned'}</b>
                                           </div>
                                       }
                            />
                        )} />
                    </Secure>
                }
                sideSecondary={() =>
                    <div className="items">
                        {(this.state.users||[]).map(user =>
                            <Fragment key={user.id}>
                                <Link to={"/user/" + user.id}>
                                    {user.shortName}: {user.firstName} {user.lastName}
                                </Link>
                                <br/>
                            </Fragment>
                        )}
                    </div>
                }
            />
        )
    }
}

export {UserApp}