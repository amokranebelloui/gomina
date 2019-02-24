// @flow

import * as React from 'react'

const LoggedUserContext = React.createContext("");

type Props = {
    permission?: string,
    condition?: (user: string) => boolean,
    children: any
}

class Secure extends React.PureComponent<Props> {
    render() {
        return (
            <LoggedUserContext.Consumer>
                {loggedUser => (
                    loggedUser &&
                    loggedUser.userId &&
                    (!this.props.permission || (loggedUser.permissions||[]).includes(this.props.permission)) &&
                    (!this.props.condition || this.props.condition(loggedUser.userId)) &&
                    this.props.children
            )}
            </LoggedUserContext.Consumer>
        )
    }
}

export { Secure, LoggedUserContext }
