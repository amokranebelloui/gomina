import React from "react"
import PropTypes from "prop-types"

class ComponentSort extends React.Component {
    constructor(props) {
        super(props)
    }
    changeSelected(sortBy) {
        this.props.onSortChanged && this.props.onSortChanged(sortBy)
    }
    render() {
        return (
            <div>
                Sort:
                <button disabled={this.props.sortBy === 'alphabetical'} onClick={e => this.changeSelected('alphabetical')}>Alphabetical</button>
                <button disabled={this.props.sortBy === 'unreleased-changes'} onClick={e => this.changeSelected('unreleased-changes')}>Unreleased Changes</button>
                <button disabled={this.props.sortBy === 'loc'} onClick={e => this.changeSelected('loc')}>LOC</button>
                <button disabled={this.props.sortBy === 'coverage'} onClick={e => this.changeSelected('coverage')}>Coverage</button>
                <button disabled={this.props.sortBy === 'last-commit'} onClick={e => this.changeSelected('last-commit')}>Last Commit</button>
                <button disabled={this.props.sortBy === 'commit-activity'} onClick={e => this.changeSelected('commit-activity')}>Commit Activity</button>
            </div>
        )
    }
}

ComponentSort.propTypes = {
    sortBy: PropTypes.string,
    onSortChanged: PropTypes.func
};

export { ComponentSort }