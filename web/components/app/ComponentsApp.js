import React from "react";
import ls from "local-storage"
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import {Secure} from "../permission/Secure"
import {ComponentHeader, ComponentSummary} from "../component/Component";
import axios from "axios/index";
import {Container} from "../common/Container";
import {Well} from "../common/Well";
import {flatMap, joinTags, splitTags} from "../common/utils";
import {ComponentSort, sortComponentsBy} from "../component/ComponentSort";
import {extendSystems} from "../system/system-utils";
import {AddComponent} from "../component/AddComponent";
import {ComponentFilter, filterComponents} from "../component/ComponentFilter";

class ComponentsApp extends React.Component {

    constructor(props) {
        super(props);
        const filter = {
            search: ls.get('components.search'),
            types: splitTags(ls.get('components.types')),
            systems: splitTags(ls.get('components.systems')),
            languages: splitTags(ls.get('components.languages')),
            tags: splitTags(ls.get('components.tags'))
        };
        this.state = {
            components: [],
            sortBy: ls.get('components.sort') || 'alphabetical',

            filter: filter
        };
        //this.retrieveSystems = this.retrieveSystems.bind(this);
        this.retrieveComponents = this.retrieveComponents.bind(this);
        console.info("componentsApp !constructor ");
    }

    /*
    retrieveSystems() {
        axios.get('/data/systems/refs')
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
        ls.set('components.sort', sortBy)
    }
    selectedComponents(components) {
        return filterComponents(components, this.state.filter);
    }
    setFilter(filter) {
        this.setState({filter: filter});
        ls.set('components.search', filter.search);
        ls.set('components.types', joinTags(filter.types));
        ls.set('components.systems', joinTags(filter.systems));
        ls.set('components.languages', joinTags(filter.languages));
        ls.set('components.tags', joinTags(filter.tags));
    }
    render() {
        const components = sortComponentsBy(this.state.components, this.state.sortBy);
        const types = flatMap(this.state.components, p => p.type ? [p.type] : [""]);
        const systems = extendSystems(flatMap(this.state.components, p => p.systems && p.systems.length > 0 ? p.systems : [""]));
        //const systems = this.state.systems;
        const languages = flatMap(this.state.components, p => p.languages && p.languages.length > 0 ? p.languages : [""]);
        const tags = flatMap(this.state.components, p => p.tags && p.tags.length > 0 ? p.tags : [""]);
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
                            <ComponentFilter filter={this.state.filter}
                                             onFilterChanged={(filter) => this.setFilter(filter)}
                                             types={types} systems={systems} languages={languages} tags={tags}
                             />
                        </Well>
                        <Well block>
                            <button onClick={e => this.retrieveComponents()}>RELOAD</button>
                            <button onClick={e => this.reloadScm()}>SCM</button>
                            <button onClick={e => this.reloadBuild()}>BUILD</button>
                            <button onClick={e => this.reloadSonar()}>SONAR</button>
                        </Well>
                        <Secure permission="component.add">
                            <Well block>
                                <AddComponent systemsContext={this.state.selectedSystems}
                                              onComponentAdded={comp => this.setState({components: [...this.state.components, comp]})} />
                            </Well>
                        </Secure>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}

export {ComponentsApp};
