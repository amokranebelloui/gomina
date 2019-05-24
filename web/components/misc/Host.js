// @flow
import React from "react";

type Props = {
    host: ?string,
    expected?: ?string
}

class Host extends React.Component<Props> {
    render() {
        const host = this.props.host ? this.props.host : this.props.expected;
        const unexpected = this.props.host && this.props.expected && this.props.expected !== this.props.host;
        const title = "Host " + (this.props.host||'?') + (unexpected && this.props.expected ? ", Expected " + this.props.expected : "");
        /*
        {unexpected && <span title="Expected host" style={{
                    marginLeft: '2px',
                    userSelect: 'all',
                    textDecoration: 'line-through'
                }}>{this.props.expected}</span>}
         */
        return (
            <span>
                <span title={title} style={{userSelect: 'all'}}>
                    {host}
                </span>
                {unexpected &&
                    <span style={{userSelect: 'all', textDecoration: 'line-through', marginLeft: '2px', opacity: 0.4}}>
                        {this.props.expected}
                    </span>
                }
            </span>
        )
    }
}

export {Host}
