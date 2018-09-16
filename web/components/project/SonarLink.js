import React from "react";
import sonarIcon from "./sonar.ico"

class SonarLink extends React.Component {
    render() {

        //const text = this.props.server ? this.props.server : "build";
        //const text = this.props.src ? this.props.src : "build";
        if (this.props.url) {
            return (
                <a href={this.props.url} target="_blank" title={this.props.url}
                   style={{borderRadius: '3px', fontSize: 10}}>
                    <img style={{verticalAlign: 'middle', display: 'inline-block'}} src={sonarIcon} width="16" height="16"/>
                </a>
            )
        }
        return null
    }
}

export {SonarLink}
