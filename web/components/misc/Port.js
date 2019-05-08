// @flow
import React from "react";

type Props = {
    port?: ?string
}

class Port extends React.Component<Props> {
    render() {
        return (
            <span style={{userSelect: 'all'}}>{this.props.port}</span>
        )
    }
}

export { Port};