import React from "react";

class Host extends React.Component {
    render() {
        const unexpected = this.props.host && this.props.expected != this.props.host;
        return (
            <span>
                <span title="Running on host" style={{userSelect: 'all'}}>{this.props.host}</span>
                {unexpected && <span title="Expected host" style={{
                    marginLeft: '2px',
                    userSelect: 'all',
                    textDecoration: 'line-through'
                }}>{this.props.expected}</span>}
            </span>
        )
    }
}

export {Host}
