// @flow
import * as React from "react"
import type {WorkType} from "./WorkType";
import {TagCloud} from "../common/TagCloud";
import {flatMap} from "../common/utils";
import {extendSystems} from "../system/system-utils";

type Props = {
    search: string,
    systems: Array<string>,
    systemsRefs: Array<string>,
    onFilterChanged?: (search: string, systems: Array<string>) => void
}

type State = {
    search: string,
    systems: Array<string>
}

class WorkFilter extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            search: this.props.search,
            systems: this.props.systems
        }
    }
    setSearch(search: string) {
        this.setState({search: search});
        this.props.onFilterChanged && this.props.onFilterChanged(search, this.state.systems)
    }
    selectSystems(values: Array<string>) {
        this.setState({systems: values});
        this.props.onFilterChanged && this.props.onFilterChanged(this.state.search, values)
    }
    render() {
        return (
            <div>
                <b>Search: </b>
                <input type="text" name="search" value={this.state.search} onChange={e => this.setSearch(e.target.value)}/>
                <br/>
                <b>Systems:</b>
                <TagCloud tags={this.props.systemsRefs} selected={this.state.systems} displayCount={false}
                          selectionChanged={values => this.selectSystems(values||[])} />
                <br/>
            </div>
        );
    }
}

function filterWork(workList: Array<WorkType>, search: string, systems: Array<string>) {
    return workList.filter(w =>
        matchesSearch(w, search) &&
        matchesList(extendSystems(flatMap(w.components, c => c.systems)), systems)
    );
}

function matchesSearch(work: WorkType, search: string) {
    let regExp = new RegExp(search, "i");
    let matchesLabel = work.label && work.label.match(regExp);
    let matchesIssues = work.issues && work.issues.find(value => value && value.issue.match(regExp));
    return matchesLabel || matchesIssues
}

function matchesList(componentValues, selectedValues) {
    if (selectedValues && selectedValues.length > 0) {
        const values = (componentValues||[]);
        return (selectedValues||[]).find(value => values.indexOf(value) !== -1);
    }
    return true
}

export { WorkFilter, filterWork }