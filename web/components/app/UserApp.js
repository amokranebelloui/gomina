// @flow
import * as React from "react"
import {AppLayout, PrimarySecondaryLayout} from "./common/layout"
import {LoggedUserContext} from "../permission/Secure"
import {Container} from "../common/Container";
import axios from "axios/index";
import Link from "react-router-dom/es/Link";

type Props = {
    match: Object // FIXME Type react match object
};
type State = {
    user: ?Object
};

class UserApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            user: null
        }
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

    componentDidMount() {
        console.info("userApp !did mount ");
        this.retrieveUser(this.props.match.params.id)
    }
    componentWillReceiveProps(nextProps: Props) {
        const newUserId = nextProps.match.params.id;
        console.info("userApp !did willRecProps ", newUserId, nextProps);
        if (this.props.match.params.id != newUserId && newUserId) {
            this.retrieveUser(newUserId)
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
                <LoggedUserContext.Consumer>
                    {loggedUser => (
                        <PrimarySecondaryLayout>
                            <Container>
                                User {this.props.match.params.id} ({loggedUser})

                                {this.state.user &&
                                <div>
                                    <br/>
                                    {this.state.user.firstName} {this.state.user.lastName}
                                </div>
                                }
                            </Container>
                            <div>
                                <Link to="/user/john.doe">John Doe</Link>
                                <br/>
                                <Link to="/user/amokrane.belloui">Amokrane Belloui</Link>
                            </div>
                        </PrimarySecondaryLayout>
                    )}
                </LoggedUserContext.Consumer>
            </AppLayout>
        )
    }
}

export {UserApp}