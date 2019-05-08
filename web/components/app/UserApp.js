// @flow
import * as React from "react"
import {AppLayout, PrimarySecondaryLayout} from "./common/layout"
import {LoggedUserContext, Secure} from "../permission/Secure"
import {Container} from "../common/Container";
import axios from "axios/index";
import Link from "react-router-dom/es/Link";
import {Knowledge} from "../knowledge/Knowledge";
import type {KnowledgeType} from "../knowledge/Knowledge";
import {Badge} from "../common/Badge";

type UserType = {
    id: string,
    login: string,
    shortName: string,
    firstName: ?string,
    lastName: ?string,
    accounts: Array<string>,
    disabled: boolean
}

type UserDataType = {
    login: string,
    shortName: string,
    firstName: ?string,
    lastName: ?string,
    accounts: Array<string>,
}

type Props = {
    match: Object // FIXME Type react match object
};
type State = {
    user: ?UserType,
    knowledge: Array<KnowledgeType>,
    users: Array<UserType>
};

class UserApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            user: null,
            knowledge: [],
            users: []
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

    componentDidMount() {
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
        /*

                                {this.state.user &&
                                <div>
                                    <br/>
                                    {this.state.user.firstName} {this.state.user.lastName}
                                </div>
                                }
                                <div>
                                    <Link to="/user/john.doe">John Doe</Link>
                                    <br/>
                                    <Link to="/user/amokrane.belloui">Amokrane Belloui</Link>
                                </div>

         */
        return (
            <AppLayout title="Components">
                <PrimarySecondaryLayout>
                    <Container>
                        {this.props.match.params.id && this.state.user &&
                            <div>
                                <b>Id: </b>{this.props.match.params.id}
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
                                    <Badge backgroundColor={'#EEEEEE'}>{account}</Badge>
                                )}
                                </span>
                                <br/>
                                <b>Knowledge</b>
                                <Knowledge knowledge={this.state.knowledge} hideUser={true} />
                                <br/>
                                <Secure condition={(user) => user === this.props.match.params.id}>
                                    <input type="button" value="Manage my profile [TODO]" />
                                </Secure>
                            </div>
                        }
                        {!this.props.match.params.id &&
                            <p>
                                See the contributors for your project,
                                their knowledge of the different areas
                                and the components they're currently working on
                            </p>
                        }
                    </Container>
                    <div>
                        <div className="items">
                        {(this.state.users||[]).map(user =>
                            [<Link key={user.id} to={"/user/" + user.id}>{user.shortName}: {user.firstName} {user.lastName}</Link>,<br key={"b" + user.id}/>]
                        )}
                        </div>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        )
    }
}

export {UserApp}