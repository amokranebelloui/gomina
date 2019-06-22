// @flow
import React from "react";
import jenkinsIcon from "./build-jenkins.png";
import {Badge} from "../common/Badge";

type Props = {
    server?: ?string,
    url?: ?string,
    job?: ?string
}

class BuildLink extends React.Component<Props> {
    constructor(props: Props) {
        super(props)
    }
    render() {
        const icon = jenkinsIcon;
        const opacity = this.props.server || this.props.job  ? null : .2;
        let title = 'server:' + (this.props.server||'?') +
            ' job:' + (this.props.job||'?') +
            ' url:' + (this.props.url||'?');
        return (
            <Badge color="white" backgroundColor="#E8BD30">
                <img style={{opacity: opacity, marginRight: '3px', marginBottom: '1px',
                    //display: 'table-cell',
                    verticalAlign: 'middle'}}
                     src={icon} width="10" height="10"/>
                <a href={this.props.url} target="_blank"
                   title={title}
                   style={{
                       //display: 'table',
                       opacity: opacity,
                       //padding: '1px',
                       borderRadius: '3px'
                   }}>
                    {this.props.server ? this.props.server : ""}
                    {this.props.job &&
                        (this.props.server ? ":" : "?:") + this.props.job
                    }
                </a>
            </Badge>
        )
    }
}

export {BuildLink}
