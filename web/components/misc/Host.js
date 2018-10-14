import React from "react";

class Host extends React.Component {
    render() {
        const host = this.props.host ? this.props.host : this.props.expected;
        const unexpected = this.props.host && this.props.expected && this.props.expected !== this.props.host;
        const title = "Running on " + this.props.host + (unexpected ? ", Expected " + this.props.expected : "");
        /*
        {unexpected && <span title="Expected host" style={{
                    marginLeft: '2px',
                    userSelect: 'all',
                    textDecoration: 'line-through'
                }}>{this.props.expected}</span>}
         */
        return (
            <span>
                <span title={title}
                      style={{userSelect: 'all', textDecoration: unexpected ? 'line-through' : null}}>
                    {host}
                </span>
            </span>
        )
    }
}

export {Host}
