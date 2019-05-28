import React from "react";


class Container extends React.Component {
    render() {
        return (
            <div style={{display: 'block', height: '100%', width: '100%', boxSizing: 'border-box', overflow: 'auto'}}>
                {this.props.children}
            </div>
        )
    }
}

export {Container}
