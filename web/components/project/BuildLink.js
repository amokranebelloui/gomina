import React from "react";

class BuildLink extends React.Component {
    render() {
        const text = this.props.server ? this.props.server : "build";
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{backgroundColor: '#E8BD30', color: 'white', padding: 2, borderRadius: '3px', fontSize: 10}}>
                {text}
            </a>
        )
    }
}

export {BuildLink}
