import React from "react";
import jenkinsIcon from "./build-jenkins.png";
import PropTypes from 'prop-types'

class BuildLink extends React.Component {
    render() {
        const icon = jenkinsIcon;
        const opacity = this.props.server || this.props.job  ? null : .2;
        let title = 'server:' + (this.props.server||'?') +
            ' job:' + (this.props.job||'?') +
            ' url:' + (this.props.url||'?');
        return (
            <a href={this.props.url} target="_blank"
               title={title}
               style={{
                   opacity: opacity,
                   backgroundColor: '#E8BD30',
                   color: 'white',
                   //padding: 2,
                   borderRadius: '3px',
                   fontSize: 10}}>
                <img style={{opacity: opacity}}
                     src={icon} width="16" height="16"/>
                <span style={{padding: 2}}>
                    {this.props.server ? this.props.server : ""}
                    {this.props.job &&
                        (this.props.server ? ":" : "?:") + this.props.job
                    }
                </span>
            </a>
        )
    }
}

BuildLink.propTypes = {
    "server": PropTypes.string,
    "url": PropTypes.string,
    "job": PropTypes.string
};

export {BuildLink}
