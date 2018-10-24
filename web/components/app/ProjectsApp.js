import React from "react";
import {AppLayout, LoggedUserContext, PrimarySecondaryLayout} from "./common/layout";
import {ProjectHeader, ProjectSummary} from "../project/Project";
import axios from "axios/index";
import {Container} from "../common/Container";
import {TagCloud} from "../common/TagCloud";
import {Well} from "../common/Well";
import {flatMap} from "../common/utils";
import {ProjectSort} from "../project/ProjectSort";
import {extendSystems} from "../system/system-utils";

class ProjectsApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            projects: [],
            sortBy: 'alphabetical'
        };
        //this.retrieveSystems = this.retrieveSystems.bind(this);
        this.retrieveProjects = this.retrieveProjects.bind(this);
        console.info("projectApp !constructor ");
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
    retrieveProjects() {
        console.log("projectApp Retr Projects ... ");
        const thisComponent = this;
        axios.get('/data/components')
            .then(response => {
                console.log("projectApp data components", response.data);
                thisComponent.setState({projects: response.data});
            })
            .catch(function (error) {
                console.log("projectApp error components", error);
                thisComponent.setState({projects: []});
            });
    }

    componentDidMount() {
        console.info("projectsApp !mount ", this.props.match.params.id);
        //this.retrieveSystems();
        this.retrieveProjects();
    }
    sortChanged(sortBy) {
        this.setState({sortBy: sortBy});
    }
    render() {
        const projects = sortProjectsBy(this.state.sortBy, this.state.projects);
        const systems = extendSystems(flatMap(this.state.projects, p => p.systems));
        //const systems = this.state.systems;
        const languages = flatMap(this.state.projects, p => p.languages);
        const tags = flatMap(this.state.projects, p => p.tags);
        return (
            <AppLayout title="Components">
            <LoggedUserContext.Consumer>
                {loggedUser => (
                    <PrimarySecondaryLayout>
                        <Container>
                            <ProjectSort sortBy={this.state.sortBy} onSortChanged={sortBy => this.sortChanged(sortBy)} />
                            <hr/>
                            <div className='project-list'>
                                <ProjectHeader />
                                {projects
                                    .filter(project => matchesSearch(project, this.state.search, this.state.selectedSystems, this.state.selectedLanguages, this.state.selectedTags))
                                    .map(project =>
                                        <ProjectSummary key={project.id} project={project} loggedUser={loggedUser} />
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


function matchesSearch(project, search, systems, languages, tags) {
    let label = (project.label || "");
    let regExp = new RegExp(search, "i");
    let matchesLabel = label.match(regExp);
    let matchesSystems = matchesList(extendSystems(project.systems), systems);
    let matchesLanguages = matchesList(project.languages, languages);
    let matchesTags = matchesList(project.tags, tags);
    //console.info("MATCH", project.id, matchesLabel, matchesSystems, matchesLanguages, matchesTags);
    return matchesLabel && matchesSystems && matchesLanguages && matchesTags
    //(languages.filter(item => item.match(regExp))||[]).length > 0 ||
    //(tags.filter(item => item.match(regExp))||[]).length > 0;
    //return project.label && project.label.indexOf(this.state.search) !== -1;
}
function matchesList(projectValues, selectedValues) {
    if (selectedValues && selectedValues.length > 0) {
        const values = (projectValues||[]);
        return (selectedValues||[]).find(value => values.indexOf(value) !== -1);
    }
    return true
}

function sortProjectsBy(sortBy, projects) {
    let result;
    switch (sortBy) {
        case 'alphabetical' :
            result = projects.sort((a, b) => a.label > b.label ? 1 : -1);
            break;
        case 'loc' :
            result = projects.sort((a, b) => (b.loc - a.loc) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'coverage' :
            result = projects.sort((a, b) => (b.coverage - a.coverage) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'last-commit' :
            result = projects.sort((a, b) => (b.lastCommit - a.lastCommit) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'commit-activity' :
            result = projects.sort((a, b) => (b.commitActivity - a.commitActivity) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'unreleased-changes' :
            result = projects.sort((a, b) => (b.changes - a.changes) * 10 + (a.label > b.label ? 1 : -1));
            break;
        default :
            result = projects
    }
    return result;
}

export {ProjectsApp};
