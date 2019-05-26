// @flow

import * as React from 'react'

type LoggedUserType = {
    userId?: ?string,
    login?: ?string,
    permissions?: Array<string>
}

const LoggedUserContext: React.Context<LoggedUserType> = React.createContext({});

type Props = {
    permission?: string,
    condition?: (user: string) => boolean,
    children?: ?any,
    fallback?: ?any
}

class Secure extends React.PureComponent<Props> {
    render() {
        return (
            <LoggedUserContext.Consumer>
                {loggedUser => (
                    loggedUser &&
                    loggedUser.userId &&
                    (!this.props.permission || (loggedUser.permissions||[]).includes(this.props.permission)) &&
                    (!this.props.condition || this.props.condition(loggedUser.userId))
                        ? this.props.children
                        : this.props.fallback
            )}
            </LoggedUserContext.Consumer>
        )
    }
}

export { Secure, LoggedUserContext }
