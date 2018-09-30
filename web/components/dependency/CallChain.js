import React from 'react'
import PropTypes  from 'prop-types'

class CallChain extends React.Component {
    constructor(props) {
        super(props)
    }
    render() {
        return (
            <Call chain={this.props.chain} padding={0} />
        )
    }
}

CallChain.propTypes = {
    chain: PropTypes.object
};


/// projectId: String, val recursive: Boolean, val functions: List<FunctionDetail> = emptyList(), val calls

class Call extends React.Component {
    constructor(props) {
        super(props)
    }
    render() {
        const chain = this.props.chain || {};
        const padding = this.props.padding || 0;
        return (
            <div>
                <span style={{paddingLeft: padding}}>{chain.projectId} {chain.recursive && '@'} {(chain.functions||[]).length}</span>,
                {(chain.calls||[]).map( call =>
                    <Call key={call.projectId} chain={call} padding={padding + 5} />
                )}
            </div>
        )
    }
}

Call.propTypes = {
    chain: PropTypes.object,
    padding: PropTypes.number
};

export { CallChain }