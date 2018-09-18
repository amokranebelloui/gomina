import React from 'react'
import PropTypes from 'prop-types'

class Revision extends React.PureComponent {
    constructor(props) {
        super(props)
    }
    overrideCopy(e) {
        console.log('override copied value', this.props.revision);
        e.clipboardData.setData('text/plain', this.props.revision);
        e.preventDefault();
    }
    render() {
        let revision = this.props.revision;
        let rev = (this.props.type === 'git' && revision) ? revision.substring(0, 7) : revision;
        return (
            <span title={revision} onCopy={e => this.overrideCopy(e)}>{rev + ' ' || '-'}</span>
        )
    }

}

Revision.propTypes = {
    type: PropTypes.string,
    revision: PropTypes.string
};

export { Revision }