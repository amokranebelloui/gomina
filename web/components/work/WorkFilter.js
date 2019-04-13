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
    let matchesJira = work.jira && work.jira.match(regExp);
    return matchesLabel || matchesJira
}

/*
function matchesSearch(component, search: string, types, systems, languages, tags) {
    let label = (component.label || "");
    let regExp = new RegExp(search, "i");
    let matchesLabel = label.match(regExp);
    let matchesTypes = matchesList([component.type], types);
    let matchesSystems = matchesList(extendSystems(component.systems), systems);
    let matchesLanguages = matchesList(component.languages, languages);
    let matchesTags = matchesList(component.tags, tags);
    //console.info("MATCH", component.id, matchesLabel, matchesSystems, matchesLanguages, matchesTags);
    return matchesLabel && matchesTypes && matchesSystems && matchesLanguages && matchesTags
    //(languages.filter(item => item.match(regExp))||[]).length > 0 ||
    //(tags.filter(item => item.match(regExp))||[]).length > 0;
    //return component.label && component.label.indexOf(this.state.search) !== -1;
}
function matchesList(componentValues, selectedValues) {
    if (selectedValues && selectedValues.length > 0) {
        const values = (componentValues||[]);
        return (selectedValues||[]).find(value => values.indexOf(value) !== -1);
    }
    return true
}
*/

export { WorkFilter, filterWork }