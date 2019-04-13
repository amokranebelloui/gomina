// @flow
import * as React from "react"
import {extendSystems} from "../system/system-utils";
import type {ComponentType} from "./ComponentType";
import {TagCloud} from "../common/TagCloud";

type ComponentFilterType = {
    search: string,
    types: Array<string>,
    systems: Array<string>,
    languages: Array<string>,
    tags: Array<string>,
}

type Props = {
    filter: ComponentFilterType,
    onFilterChanged?: ComponentFilterType => void,
    types: Array<string>,
    systems: Array<string>,
    languages: Array<string>,
    tags: Array<string>,
}

type State = {
    search: string,
    types: Array<string>,
    systems: Array<string>,
    languages: Array<string>,
    tags: Array<string>,
}

class ComponentFilter extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            search: this.props.filter.search,
            types: this.props.filter.types,
            systems: this.props.filter.systems,
            languages: this.props.filter.languages,
            tags: this.props.filter.tags
        }
    }
    setFilter(search: string) {
        this.setState({search: search});
        this.notifyChange({search: search})
    }
    selectTypes(values: Array<string>) {
        this.setState({types: values});
        this.notifyChange({types: values});
    }
    selectSystems(values: Array<string>) {
        this.setState({systems: values});
        this.notifyChange({systems: values});
    }
    selectLanguages(values: Array<string>) {
        this.setState({languages: values});
        this.notifyChange({languages: values});
    }
    selectTags(values: Array<string>) {
        this.setState({tags: values});
        this.notifyChange({tags: values});
    }

    notifyChange(delta: any) {
        const fromState = {
            search: this.state.search,
            types: this.state.types,
            systems: this.state.systems,
            languages: this.state.languages,
            tags: this.state.tags
        };
        const filter = Object.assign({}, fromState, delta);
        this.props.onFilterChanged && this.props.onFilterChanged(filter)
    }

    render() {
        return (
            <div>
                <b>Search: </b>
                <input type="text" name="search" value={this.state.search} onChange={e => this.setFilter(e.target.value)}/>
                &nbsp;
                <br/>
                <b>Types:</b>
                <TagCloud tags={this.props.types} selected={this.state.types} displayCount={true}
                          selectionChanged={values => this.selectTypes(values||[])} />
                <br/>
                <b>Systems:</b>
                <TagCloud tags={this.props.systems} selected={this.state.systems} displayCount={true}
                          selectionChanged={values => this.selectSystems(values||[])} />
                <br/>
                <b>Languages:</b>
                <TagCloud tags={this.props.languages} selected={this.state.languages} displayCount={true}
                          selectionChanged={values => this.selectLanguages(values||[])} />
                <br/>
                <b>Tags:</b>
                <TagCloud tags={this.props.tags} selected={this.state.tags} displayCount={true}
                          selectionChanged={values => this.selectTags(values||[])} />
                <br/>
            </div>
        );
    }
}
function filterComponents(components: Array<ComponentType>, filter: ComponentFilterType): Array<ComponentType>  {
    return components.filter(component =>
        matchesSearch(component, filter.search, filter.types, filter.systems, filter.languages, filter.tags)
    );
}


function matchesSearch(component: ComponentType, search: string,
                       types: Array<string>, systems: Array<string>,
                       languages: Array<string>, tags: Array<string>) {
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

export { ComponentFilter, filterComponents }