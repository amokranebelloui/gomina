// @flow
import React, {Fragment} from "react"
import axios from "axios/index";
import ls from "local-storage"
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import Link from "react-router-dom/es/Link";
import {compareVersions, compareVersionsReverse} from "../common/version-utils";
import type {ComponentRefType} from "../component/ComponentType";
import {Well} from "../common/Well";
import type {InstanceRefType} from "../environment/InstanceType";
import {Badge} from "../common/Badge";
import {groupingBy} from "../common/utils";
import type {EnvType} from "../environment/Environment";
import type {ComponentVersionType, LibraryType, LibraryUsageType} from "../library/LibraryType";
import {LibraryCatalog} from "../library/Libraries";
import {filterLibraries, LibraryFilter} from "../library/LibraryFilter";
import "../common/items.css"

type Props = {
    match: any
}

type SortDirection = 'ascending'|'descending'

type State = {
    search: string,
    envs: Array<EnvType>,
    libraries: Array<LibraryType>,
    libraryId: ?string,
    library: Array<LibraryUsageType>,
    sort: SortDirection
}

class LibrariesApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            search: ls.get('libraries.search'),
            envs: [],
            libraries: [],
            libraryId: this.props.match.params.id,
            library: [],
            sort: ls.get('libraries.sort') || 'ascending'
        }
    }
    retrieveEnvs() {
        const thisComponent = this;
        axios.get('/data/envs')
            .then(response => thisComponent.setState({envs: response.data}))
            .catch(error => thisComponent.setState({envs: []}));
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
        const factor = this.state.sort === 'ascending' ? 1 : -1;
        return libraryUsage.sort((a, b) => factor * compareVersions(a.version, b.version))
    }
    changeSearch(e: string) {
        this.setState({"search": e});
        ls.set('libraries.search', e)
    }
    changeSort(sort: SortDirection) {
        this.setState({"sort": sort});
        ls.set('libraries.sort', sort)
    }
    componentWillReceiveProps(nextProps: Props) {
        const newLibraryId = nextProps.match.params.id;
        this.setState({libraryId: newLibraryId});
        if (this.props.match.params.id !== newLibraryId) {
            this.retrieveLibrary(newLibraryId)
        }
    }
    componentDidMount() {
        this.retrieveEnvs();

        this.retrieveLibraries();
        if (this.props.match.params.id) {
            this.retrieveLibrary(this.props.match.params.id);
        }
    }
    groupByComponent(components: Array<ComponentVersionType>): Array<{component: ComponentRefType, usages: Array<ComponentVersionType>}> {
        const map = components && groupingBy(components, c => c.component.id) || {};
        const result = Object.keys(map)
            .map(c => {
                return {component: map[c][0].component, usages: map[c] || []}}
            );
        return result || [];
    }
    groupByEnv(instances: Array<InstanceRefType>): Array<{env: string, count: number}> {
        const map = instances && groupingBy(instances, i => i.env) || {};
        const result = Object.keys(map).map(env => { return {'env': env, count: map[env].length}}) ;
        return result;
    }
    render() {
        const libraries = filterLibraries(this.state.libraries, this.state.search) || [];
        const totalCount = this.state.libraries && this.state.libraries.length || 0;
        const envs = {};
        this.state.envs.forEach(e => envs[e.env] = e);
        const color = e => {
            const env = envs[e];
            return env && (env.type === 'PROD' ? '#FFFFFF' : env.type === 'TEST' ? 'black' : 'black')
        };
        const backgroundColor = e => {
            const env = envs[e];
            return env && (env.type === 'PROD' ? '#a9c8d4' : env.type === 'TEST' ? '#c3cfd4' : 'lightgray')
        };

        return (
            <AppLayout title="Libraries">
                <PrimarySecondaryLayout>
                    <div>
                        {this.state.libraryId && this.state.library &&
                            <Fragment>
                                <h3 style={{display: 'inline-block'}}>Usage of '{this.state.libraryId}'</h3>
                                <div style={{float: 'right'}}>
                                    <button onClick={e => this.changeSort('ascending')}
                                            style={{padding: '2px', color: this.state.sort === 'ascending' ? 'gray' : null}}>
                                        OLD FIRST
                                    </button>
                                    <button onClick={e => this.changeSort('descending')}
                                            style={{padding: '2px', color: this.state.sort === 'descending' ? 'gray' : null}}>
                                        RECENT FIRST
                                    </button>
                                </div>
                                <br/>
                                <table width="100%">
                                    <tbody>
                                    {this.sortLibraryUsage(this.state.library).map(libUsage =>
                                        <tr>
                                            <td valign="top" style={{
                                                textAlign: 'right', minWidth: '40px', padding: '0px 5px',
                                                borderBottom: '1px solid lightgray',
                                                borderRight: '1px solid lightgray',
                                                whiteSpace: 'nowrap'
                                            }}>
                                                <b>{libUsage.version}</b>
                                            </td>
                                            <td width="100%" style={{borderBottom: '1px solid lightgray'}}>
                                            {this.groupByComponent(libUsage.components).map(usage =>
                                                <Fragment>
                                                    <Link to={'/component/' + usage.component.id}>{usage.component.label} </Link>
                                                    <br/>
                                                    {usage.usages.map(c =>
                                                        <span className="items" style={{paddingLeft: '20px'}}>
                                                            {c.instances && c.instances.length > 0 ?
                                                                <Fragment>
                                                                    <Badge>{c.version}</Badge>
                                                                    {this.groupByEnv(c.instances).map(env =>
                                                                        <Fragment>
                                                                            <span>
                                                                                <Badge key={env.env}
                                                                                       title={c.instances.filter(i => i.env === env.env).map(instance => instance.name).join(' ')}
                                                                                       backgroundColor={backgroundColor(env.env)}
                                                                                       color={color(env.env)}>
                                                                                    {env.env} ({env.count})
                                                                                </Badge>
                                                                            </span>
                                                                        </Fragment>
                                                                    )}
                                                                </Fragment>
                                                            :
                                                                <Badge><i style={{color: 'lightgray'}}>{c.version}</i></Badge>
                                                            }
                                                            &nbsp;

                                                            <br/>
                                                        </span>
                                                    )}
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
                            <LibraryFilter search={this.state.search}
                                           onChange={e => this.changeSearch(e)} />
                        </Well>
                        <Well block>
                            <LibraryCatalog libraries={libraries} totalCount={totalCount} />
                        </Well>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}

export { LibrariesApp }