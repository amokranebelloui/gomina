import React from "react";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import {LoggedUserContext, Secure} from "../permission/Secure"
import {ComponentHeader, ComponentSummary} from "../component/Component";
import axios from "axios/index";
import {Container} from "../common/Container";
import {TagCloud} from "../common/TagCloud";
import {Well} from "../common/Well";
import {flatMap} from "../common/utils";
import {ComponentSort} from "../component/ComponentSort";
import {extendSystems} from "../system/system-utils";
import {AddComponent} from "../component/AddComponent";

class ComponentsApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            components: [],
            sortBy: 'alphabetical'
        };
        //this.retrieveSystems = this.retrieveSystems.bind(this);
        this.retrieveComponents = this.retrieveComponents.bind(this);
        console.info("componentsApp !constructor ");
    }
    /*
    retrieveSystems() {
        axios.get('/data/components/systems')
            .then(response => {
                console.log("systems data", response.data);
                this.setState({systems: response.data});
            })
            .catch(function (error) {
                console.log("systems data error", error);
            });
    }
    */
    retrieveComponents() {
        console.log("componentsApp Retr Components ... ");
        const thisComponent = this;
        axios.get('/data/components')
            .then(response => {
                console.log("componentsApp data components", response.data);
                thisComponent.setState({components: response.data});
            })
            .catch(function (error) {
                console.log("componentsApp error components", error);
                thisComponent.setState({components: []});
            });
    }
    reloadScm() {
        const componentIds = this.selectedComponents(this.state.components).map(c => c.id).join(',');
        console.info("Reloading", componentIds);
        axios.put('/data/components/reload-scm?componentIds=' + componentIds)
            .then(response => {
                console.log("component reloaded", response.data);
                this.retrieveComponents()
            })
            .catch(function (error) {
                console.log("component reload error", error.response);
            });
    }
    reloadBuild() {
        const componentIds = this.selectedComponents(this.state.components).map(c => c.id).join(',');
        console.info("Reloading build ", componentIds);
        axios.put('/data/components/reload-build?componentIds=' + componentIds)
            .then(response => {
                console.log("component build reloaded", response.data);
                this.retrieveComponents()
            })
            .catch(function (error) {
                console.log("component build reload error", error.response);
            });
    }
    reloadSonar() {
        const componentIds = this.selectedComponents(this.state.components).map(c => c.id).join(',');
        axios.put('/data/components/reload-sonar?componentIds=' + componentIds)
            .then(response => {
                console.log("sonar reloaded", response.data);
                this.retrieveComponents()
            })
            .catch(function (error) {
                console.log("sonar reload error", error.response);
            });
    }

    componentDidMount() {
        console.info("componentsApp !mount ", this.props.match.params.id);
        //this.retrieveSystems();
        this.retrieveComponents();
    }
    sortChanged(sortBy) {
        this.setState({sortBy: sortBy});
    }
    selectedComponents(components) {
        const selectedComponents = components
            .filter(component => matchesSearch(component, this.state.search, this.state.selectedTypes, this.state.selectedSystems, this.state.selectedLanguages, this.state.selectedTags));
        return selectedComponents;
    }
    render() {
        const components = sortComponentsBy(this.state.sortBy, this.state.components);
        const types = flatMap(this.state.components, p => [p.type]);
        const systems = extendSystems(flatMap(this.state.components, p => p.systems));
        //const systems = this.state.systems;
        const languages = flatMap(this.state.components, p => p.languages);
        const tags = flatMap(this.state.components, p => p.tags);
        return (
            <AppLayout title="Components">
                <PrimarySecondaryLayout>
                    <Container>
                        <ComponentSort sortBy={this.state.sortBy} onSortChanged={sortBy => this.sortChanged(sortBy)} />
                        <hr/>
                        <div className='component-list'>
                            <ComponentHeader />
                            {this.selectedComponents(components).map(component =>
                                <ComponentSummary key={component.id} component={component}/>
                            )}
                        </div>
                    </Container>
                    <div>
                        <Well block>
                            Search:
                            <input type="text" name="search" onChange={e => this.setState({search: e.target.value})}/>
                            &nbsp;
                            <br/>
                            <b>Types:</b>
                            <TagCloud tags={types} displayCount={true}
                                      selectionChanged={values => this.setState({selectedTypes: values})} />
                            <br/>
                            <b>Systems:</b>
                            <TagCloud tags={systems} displayCount={true}
                                      selectionChanged={values => this.setState({selectedSystems: values})} />
                            <br/>
                            <b>Languages:</b>
                            <TagCloud tags={languages} displayCount={true}
                                      selectionChanged={values => this.setState({selectedLanguages: values})} />
                            <br/>
                            <b>Tags:</b>
                            <TagCloud tags={tags} displayCount={true}
                                      selectionChanged={values => this.setState({selectedTags: values})} />
                            <br/>
                        </Well>
                        <Well block>
                            <button onClick={e => this.retrieveComponents()}>RELOAD</button>
                            <button onClick={e => this.reloadScm()}>SCM</button>
                            <button onClick={e => this.reloadBuild()}>BUILD</button>
                            <button onClick={e => this.reloadSonar()}>SONAR</button>
                        </Well>
                        <Secure permission="component.add">
                            <Well block>
                                <AddComponent />
                            </Well>
                        </Secure>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}


function matchesSearch(component, search, types, systems, languages, tags) {
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

function sortComponentsBy(sortBy, components) {
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
        default :
            result = components
    }
    return result;
}

export {ComponentsApp};
