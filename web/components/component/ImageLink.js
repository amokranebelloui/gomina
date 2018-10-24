import React from "react";

class ImageLink extends React.Component {
    render() {
        const text = this.props.server ? this.props.server : "build";
        //const text = this.props.src ? this.props.src : "build";
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{padding: 2, borderRadius: '3px', fontSize: 10}}>
                <img src={this.props.src} width="16" height="16"/>
            </a>
        )
    }
}

export {ImageLink}
