import React from "react";
import {AppLayout, LoggedUserContext, PrimarySecondaryLayout} from "./common/layout";
import {ComponentHeader, ComponentSummary} from "../component/Component";
import axios from "axios/index";
import {Container} from "../common/Container";
import {TagCloud} from "../common/TagCloud";
import {Well} from "../common/Well";
import {flatMap} from "../common/utils";
import {ComponentSort} from "../component/ComponentSort";
import {extendSystems} from "../system/system-utils";

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

    componentDidMount() {
        console.info("componentsApp !mount ", this.props.match.params.id);
        //this.retrieveSystems();
        this.retrieveComponents();
    }
    sortChanged(sortBy) {
        this.setState({sortBy: sortBy});
    }
    render() {
        const components = sortComponentsBy(this.state.sortBy, this.state.components);
        const systems = extendSystems(flatMap(this.state.components, p => p.systems));
        //const systems = this.state.systems;
        const languages = flatMap(this.state.components, p => p.languages);
        const tags = flatMap(this.state.components, p => p.tags);
        return (
            <AppLayout title="Components">
            <LoggedUserContext.Consumer>
                {loggedUser => (
                    <PrimarySecondaryLayout>
                        <Container>
                            <ComponentSort sortBy={this.state.sortBy} onSortChanged={sortBy => this.sortChanged(sortBy)} />
                            <hr/>
                            <div className='component-list'>
                                <ComponentHeader />
                                {components
                                    .filter(component => matchesSearch(component, this.state.search, this.state.selectedSystems, this.state.selectedLanguages, this.state.selectedTags))
                                    .map(component =>
                                        <ComponentSummary key={component.id} component={component} loggedUser={loggedUser} />
                                    )
                                }
                            </div>
                        </Container>
                        <div>
                            <Well block>
                                Search:
                                <input type="text" name="search" onChange={e => this.setState({search: e.target.value})}/>
                                &nbsp;
                                <br/>
                                Systems:
                                <TagCloud tags={systems} displayCount={true}
                                          selectionChanged={values => this.setState({selectedSystems: values})} />
                                <br/>
                                Languages:
                                <TagCloud tags={languages} displayCount={true}
                                          selectionChanged={values => this.setState({selectedLanguages: values})} />
                                <br/>
                                Tags:
                                <TagCloud tags={tags} displayCount={true}
                                          selectionChanged={values => this.setState({selectedTags: values})} />
                                <br/>
                            </Well>
                        </div>
                    </PrimarySecondaryLayout>
                )}
            </LoggedUserContext.Consumer>
            </AppLayout>
        );
    }
}


function matchesSearch(component, search, systems, languages, tags) {
    let label = (component.label || "");
    let regExp = new RegExp(search, "i");
    let matchesLabel = label.match(regExp);
    let matchesSystems = matchesList(extendSystems(component.systems), systems);
    let matchesLanguages = matchesList(component.languages, languages);
    let matchesTags = matchesList(component.tags, tags);
    //console.info("MATCH", component.id, matchesLabel, matchesSystems, matchesLanguages, matchesTags);
    return matchesLabel && matchesSystems && matchesLanguages && matchesTags
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
