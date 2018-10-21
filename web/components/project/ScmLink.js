import React from "react";

import PropTypes from 'prop-types'
import svnIcon from "./scm-svn.png"
import gitIcon from "./scm-git.png"
import mercurialIcon from "./scm-mercurial.png"
import dummyIcon from "./scm-dummy.png"
import defaultIcon from "./scm.png"

function iconForType(type) {
    switch (type) {
        case "svn" : return svnIcon;
        case "git" : return gitIcon;
        case "mercurial" : return mercurialIcon;
        case "dummy" : return dummyIcon;
        default : return defaultIcon;
    }
}

class ScmLink extends React.Component {
    render() {
        let icon = iconForType(this.props.type);
        const changesCount = this.props.changes;
        const hasChanges = changesCount > 0;
        //const backgroundColor = hasChanges ? '#EAA910' : '#F2E18F';
        // TODO Display project has unreleased changes
        const opacity = this.props.url ? null : .05;
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{
                   /*height: '100%',*/
                   //backgroundColor: backgroundColor,
                   //color: 'white',
                   //padding: 2,
                   //borderRadius: '3px',
                   //fontSize: 10
               }}>
                <img src={icon} width="16" height="16"
                     style={{verticalAlign: 'middle', opacity: opacity}} />
            </a>
        )
    }
}

ScmLink.propTypes = {
    type: PropTypes.string,
    url: PropTypes.string
};

ScmLink.defaultProps = {
    //type: "svn"
};

export {ScmLink}
