// @flow

import * as React from 'react'

const LoggedUserContext = React.createContext("");

type Props = {
    children: any
}

class Secure extends React.PureComponent<Props> {
    render() {
        return (
            <LoggedUserContext.Consumer>
                {loggedUser => (
                    loggedUser && this.props.children
            )}
            </LoggedUserContext.Consumer>
        )
    }
}

export { Secure, LoggedUserContext }
