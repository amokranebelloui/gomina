import React from "react";

class ScmLink extends React.Component {
    render() {
        const changesCount = this.props.changes;
        const hasChanges = changesCount > 0;
        const backgroundColor = hasChanges ? '#EAA910' : '#F2E18F';
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{
                   backgroundColor: backgroundColor,
                   color: 'white',
                   padding: 2,
                   borderRadius: '3px',
                   fontSize: 10
               }}>
                svn
            </a>
        )
    }
}

export {ScmLink}
