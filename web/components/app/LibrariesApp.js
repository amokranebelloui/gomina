// @flow
import React, {Fragment} from "react"
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import Link from "react-router-dom/es/Link";
import {compareVersions, compareVersionsReverse} from "../common/version-utils";
import type {ComponentRefType} from "../component/ComponentType";
import {Well} from "../common/Well";

type LibraryType = {
    artifactId: string,
    versions: Array<string>
}

type ComponentVersionType = {
    component: ComponentRefType,
    version: string
}

type LibraryUsageType = {
    version: string,
    components: Array<ComponentVersionType>
}

type Props = {
    match: any
}

type State = {
    search: string,
    libraries: Array<LibraryType>,
    libraryId: ?string,
    library: Array<LibraryUsageType>
}

class LibrariesApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            search: '',
            libraries: [],
            libraryId: this.props.match.params.id,
            library: []
        }
    }
    retrieveLibraries() {
        const thisComponent = this;
        axios.get('/data/dependencies/libraries')
            .then(response => thisComponent.setState({libraries: response.data}))
            .catch(error => thisComponent.setState({libraries: []}));
    }
    retrieveLibrary(libraryId: string) {
        const thisComponent = this;
        axios.get('/data/dependencies/library/' + libraryId)
            .then(response => thisComponent.setState({library: response.data}))
            .catch(error => thisComponent.setState({library: []}));
    }
    sortLibraryUsage(libraryUsage: Array<LibraryUsageType>): Array<LibraryUsageType> {
        return libraryUsage.sort((a, b) => compareVersions(b.version, a.version))
    }
    matchesSearch(library: LibraryType) {
        return library.artifactId.match(new RegExp(this.state.search, "i"));
    }
    componentWillReceiveProps(nextProps: Props) {
        const newLibraryId = nextProps.match.params.id;
        this.setState({libraryId: newLibraryId});
        if (this.props.match.params.id !== newLibraryId) {
            this.retrieveLibrary(newLibraryId)
        }
    }
    componentDidMount() {
        this.retrieveLibraries();
        if (this.props.match.params.id) {
            this.retrieveLibrary(this.props.match.params.id);
        }
    }
    render() {
        const libraries = (this.state.libraries || []).filter(l => this.matchesSearch(l));
        return (
            <AppLayout title="Libraries">
                <PrimarySecondaryLayout>
                    <div>
                        {this.state.libraryId && this.state.library &&
                            <Fragment>
                                <h3>Usage of '{this.state.libraryId}'</h3>
                                <br/>
                                <table>
                                    <tbody>
                                    {this.sortLibraryUsage(this.state.library).map(libUsage =>
                                        <tr>
                                            <td valign="top" style={{textAlign: 'right', minWidth: '40px', padding: '0px 5px'}}>
                                                {libUsage.version}
                                            </td>
                                            <td>
                                                {libUsage.components.map(c =>
                                                    <Fragment>
                                                        <Link to={'/component/' + c.component.id}>{c.component.label} </Link>
                                                        <i style={{color: 'lightgray'}}>{c.version}</i>
                                                        <br/>
                                                    </Fragment>
                                                )}
                                            </td>
                                        </tr>
                                    )}
                                    </tbody>
                                </table>
                            </Fragment>
                        }
                        {!(this.state.libraryId && this.state.library) &&
                            <div>
                                <p>
                                    Understand the dependencies to libraries and their different versions
                                    <br/>
                                    <b>WILL ALSO INCLUDE A VIEW OF THE RUNTIME</b>
                                </p>
                                <br/>
                                {libraries.map(l =>
                                    <div>{l.artifactId} {l.versions.sort(compareVersionsReverse).map(v => <b>{v}&nbsp;&nbsp;</b>)}</div>
                                )}
                            </div>
                        }
                    </div>
                    <div>
                        <Well block>
                            <b>Search: </b>
                            <input type="text" name="search"
                                   value={this.state.search}
                                   onChange={e => this.setState({"search": e.target.value})}
                                   autoComplete="off"
                            />
                        </Well>
                        <Well block>
                        {libraries.map(l =>
                            <div>
                                <Link to={'/library/' + l.artifactId}>{l.artifactId}</Link>
                                &nbsp;&nbsp;
                                {l.versions.sort(compareVersionsReverse).map(v => <b>{v}&nbsp;&nbsp;</b>)}</div>
                        )}
                        </Well>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}

export { LibrariesApp }