import React from "react"
import PropTypes from "prop-types"
import type {KnowledgeType} from "../knowledge/Knowledge";
import type {ComponentType} from "./ComponentType";
import {groupBy} from "../common/utils";

class ComponentSort extends React.Component {
    constructor(props) {
        super(props)
    }
    changeSelected(sortBy) {
        this.props.onSortChanged && this.props.onSortChanged(sortBy)
    }
    render() {
        return (
            <div className="items">
                Sort:
                <button disabled={this.props.sortBy === 'alphabetical'} onClick={e => this.changeSelected('alphabetical')}>Alphabetical</button>
                <button disabled={this.props.sortBy === 'unreleased-changes'} onClick={e => this.changeSelected('unreleased-changes')}>Unreleased Changes</button>
                <button disabled={this.props.sortBy === 'loc'} onClick={e => this.changeSelected('loc')}>LOC</button>
                <button disabled={this.props.sortBy === 'coverage'} onClick={e => this.changeSelected('coverage')}>Coverage</button>
                <button disabled={this.props.sortBy === 'last-commit'} onClick={e => this.changeSelected('last-commit')}>Last Commit</button>
                <button disabled={this.props.sortBy === 'commit-activity'} onClick={e => this.changeSelected('commit-activity')}>Commit Activity</button>
                <button disabled={this.props.sortBy === 'knowledge'} onClick={e => this.changeSelected('knowledge')}>Knowledge</button>
            </div>
        )
    }
}

ComponentSort.propTypes = {
    sortBy: PropTypes.string,
    onSortChanged: PropTypes.func
};

function k(knowledgeMap, component) {
    return knowledgeMap[component.id] || 0
}

function sortComponentsBy(components: Array<ComponentType>, knowledge: Array<KnowledgeType>, sortBy) {
    const knowledgeMap = {};
    knowledge && knowledge.forEach(k => knowledgeMap[k.component.id] = k.knowledge);
    let result;
    switch (sortBy) {
        case 'alphabetical' :
            result = components.sort((a, b) => a.label > b.label ? 1 : -1);
            break;
        case 'loc' :
            result = components.sort((a, b) => (b.loc - a.loc) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'coverage' :
            result = components.sort((a, b) => (b.coverage - a.coverage) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'last-commit' :
            result = components.sort((a, b) => (b.lastCommit - a.lastCommit) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'commit-activity' :
            result = components.sort((a, b) => (b.commitActivity - a.commitActivity) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'unreleased-changes' :
            result = components.sort((a, b) => (b.changes - a.changes) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'knowledge' :
            result = components.sort((a, b) => (k(knowledgeMap, b) - k(knowledgeMap, a)) * 10 + (a.label > b.label ? 1 : -1));
            break;
        default :
            result = components
    }
    return result;
}

export { ComponentSort, sortComponentsBy }