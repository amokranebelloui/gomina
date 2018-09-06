import React from "react";

class Port extends React.Component {
    render() {
        return (
            <span style={{userSelect: 'all'}}>{this.props.port}</span>
        )
    }
}

export { Port};