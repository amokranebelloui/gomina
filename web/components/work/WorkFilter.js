// @flow
import * as React from "react"
import type {WorkType} from "./WorkType";

type Props = {
    search: string,
    onFilterChanged?: string => void
}

type State = {
    search: string
}

class WorkFilter extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            search: this.props.search
        }
    }
    setSearch(search: string) {
        this.setState({search: search});
        this.props.onFilterChanged && this.props.onFilterChanged(search)
    }
    render() {
        return (
            <div>
                <b>Search: </b>
                <input type="text" name="search" value={this.state.search} onChange={e => this.setSearch(e.target.value)}/>
            </div>
        );
    }
}

function filterWork(workList: Array<WorkType>, search: string) {
    return workList.filter(w => matchesSearch(w, search));
}

function matchesSearch(work: WorkType, search: string) {
    let regExp = new RegExp(search, "i");
    let matchesLabel = work.label && work.label.match(regExp);
    let matchesIssues = work.issues && work.issues.find(value => value && value.issue.match(regExp));
    return matchesLabel || matchesIssues
}

export { WorkFilter, filterWork }