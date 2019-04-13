// @flow
import React from "react"
import type {WorkType} from "./WorkType";

type Props = {
    sortBy: string,
    onSortChanged: string => void
}

class WorkSort extends React.Component<Props> {
    constructor(props: Props) {
        super(props)
    }
    changeSelected(sortBy: string) {
        this.props.onSortChanged && this.props.onSortChanged(sortBy)
    }
    render() {
        return (
            <div>
                Sort:
                <button disabled={this.props.sortBy === 'due-date'} onClick={e => this.changeSelected('due-date')}>Due</button>
                <button disabled={this.props.sortBy === 'creation-date'} onClick={e => this.changeSelected('creation-date')}>Creation</button>
                <button disabled={this.props.sortBy === 'alphabetical'} onClick={e => this.changeSelected('alphabetical')}>Label</button>
            </div>
        )
    }
}

function sortWorkBy(workList: Array<WorkType>, sortBy: string): Array<WorkType> {
    if (workList) {
        switch (sortBy) {
            case 'alphabetical' :
                return workList.sort((a, b) => compare(a.label, b.label));
            case 'creation-date' :
                return workList.sort((a, b) => compare(b.creationDate, a.creationDate) * 10 + compare(a.label, b.label));
            case 'due-date' :
                return workList.sort((a, b) => compare(b.dueDate, a.dueDate) * 100 + compare(b.creationDate, a.creationDate) * 10 + compare(a.label, b.label));
        }
    }
    return workList || [];
}

function compare(a: ?any, b: ?any): number {
    return a === b ? 0 : a != null && b != null ? (a > b ? 1 : -1) : a != null ? 1 : b != null ? -1 : 0;
}

export { WorkSort, sortWorkBy }