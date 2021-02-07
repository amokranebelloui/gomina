// @flow
import React, {Fragment} from "react"
import axios from "axios/index";
import ls from "local-storage"
import Link from "react-router-dom/es/Link";
import {compareVersions} from "../components/common/version-utils";
import type {ComponentRefType} from "../components/component/ComponentType";
import type {InstanceRefType} from "../components/environment/InstanceType";
import {Badge} from "../components/common/Badge";
import {groupingBy} from "../components/common/utils";
import type {EnvType} from "../components/environment/Environment";
import type {ComponentVersionType, LibraryType, LibraryUsageType} from "../components/library/LibraryType";
import {LibraryCatalog} from "../components/library/Libraries";
import {filterLibraries, LibraryFilter} from "../components/library/LibraryFilter";
import "../components/common/items.css"
import {LibrariesSummary} from "../components/library/LibrariesSummary";
import {ApplicationLayout} from "./common/ApplicationLayout";

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
    showUnused: boolean,
    showVersions: boolean,
    showEnv: ?string,
    filterComponent: string,
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
            filterComponent: '',
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
            .then(response => {
                document.title = libraryId + ' - Library';
                thisComponent.setState({library: response.data})
            })
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
        document.title = 'Libraries';
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
    render() {
        const libraries = filterLibraries(this.state.libraries, this.state.search) || [];
        const totalCount = this.state.libraries && this.state.libraries.length || 0;

        const libraryUsage = this.sortLibraryUsage(this.state.library)
            .map(libraryUsage => ({
                    "version": libraryUsage.version,
                    "dismissed": libraryUsage.dismissed,
                    "components": libraryUsage.components
                        .filter(c => c.component.label && c.component.label.match(RegExp(this.state.filterComponent, 'i')))
                        .map(u => ({
                            "component": u.component,
                            "version": u.version,
                            "instances": u.instances.filter(i => !this.state.showEnv || i.env === this.state.showEnv),
                        }))
                        .filter(c => this.state.showVersions || c.instances.length > 0)
                })
            )
            .filter(libraryUsage => this.state.showUnused || libraryUsage.components.length > 0);
        return (
            <ApplicationLayout title="Libraries 1"
               header={() =>
                   <Fragment>
                       <h3 style={{display: 'inline-block'}}>Usage of '{this.state.libraryId}'</h3>
                       <div style={{float: 'right'}}>
                           Show unused
                           <input type="checkbox" checked={this.state.showUnused} onChange={e => this.setState({showUnused: e.target.checked})} />
                           &nbsp;
                           Show versions
                           <input type="checkbox" checked={this.state.showVersions} onChange={e => this.setState({showVersions: e.target.checked})} />
                           &nbsp;
                           Envs
                           <select name="env" value={this.state.showEnv}
                                   onChange={e => this.setState({showEnv: e.target.value})}>
                               <option value=""></option>
                               {this.state.envs.map(e =>
                                   <option value={e.env}>{e.env}</option>
                               )}
                           </select>
                           &nbsp;
                           Component:
                           <input type="text" value={this.state.filterComponent} onChange={e => this.setState({filterComponent: e.target.value})} />
                           &nbsp;
                           <button onClick={e => this.changeSort('ascending')}
                                   style={{padding: '2px', color: this.state.sort === 'ascending' ? 'gray' : null}}>
                               OLD FIRST
                           </button>
                           <button onClick={e => this.changeSort('descending')}
                                   style={{padding: '2px', color: this.state.sort === 'descending' ? 'gray' : null}}>
                               RECENT FIRST
                           </button>
                       </div>
                       <div style={{clear: 'both'}} />
                   </Fragment>
               }
               main={()=>
                   <Fragment>
                       {this.state.libraryId && this.state.library &&
                       <Fragment>

                           <table width="100%">
                               <tbody>
                               {libraryUsage.map(libUsage =>
                                   <tr key={libUsage.version}>
                                       <td valign="top" style={{
                                           textAlign: 'right', minWidth: '40px', padding: '0px 5px',
                                           borderBottom: '1px solid lightgray',
                                           borderRight: '1px solid lightgray',
                                           whiteSpace: 'nowrap'
                                       }}>
                                           <b style={{textDecoration: libUsage.dismissed ? "line-through": null}}>{libUsage.version}</b>
                                       </td>
                                       <td width="100%" style={{borderBottom: '1px solid lightgray'}}>
                                           {this.groupByComponent(libUsage.components).map(usage =>
                                               <Fragment>
                                                   <ComponentVersions usage={usage} envs={this.state.envs} sort={this.state.sort} />
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
                           <p>Understand the dependencies to libraries and their different versions.</p>
                           <br/>
                           <LibrariesSummary libraries={this.state.libraries} />
                       </div>
                       }
                   </Fragment>
               }
               sidePrimary={()=>
                   <LibraryFilter search={this.state.search}
                                  onChange={e => this.changeSearch(e)} />
               }
               sideSecondary={()=>
                   <LibraryCatalog libraries={libraries} totalCount={totalCount} />
               }
            />
        );
    }
}

type ComponentVersionsProps = {
    usage: {component: ComponentRefType, usages: Array<ComponentVersionType>},
    envs: Array<EnvType>,
    sort: SortDirection
}

class ComponentVersions extends React.Component<ComponentVersionsProps> {
    constructor(props: ComponentVersionsProps) {
        super(props);
    }
    groupByEnv(instances: Array<InstanceRefType>): Array<{env: string, count: number}> {
        const map = instances && groupingBy(instances, i => i.env) || {};
        const result = Object.keys(map).map(env => { return {'env': env, count: map[env].length}}) ;
        return result;
    }
    render() {
        const usage = this.props.usage;
        const envs = {};
        this.props.envs.forEach(e => envs[e.env] = e);
        const color = e => {
            const env = envs[e];
            return env && (env.type === 'PROD' ? '#FFFFFF' : env.type === 'TEST' ? 'black' : 'black')
        };
        const backgroundColor = e => {
            const env = envs[e];
            return env && (env.type === 'PROD' ? '#a9c8d4' : env.type === 'TEST' ? '#c3cfd4' : 'lightgray')
        };
        const factor = this.props.sort === 'ascending' ? 1 : -1;
        const usages = usage.usages.sort((a,b) => factor * compareVersions(a.version, b.version));
        return (
            <Fragment key={usage.component.id}>
                <Link to={'/component/' + usage.component.id}>{usage.component.label} </Link>
                <span className="items">
                {usages.map(c =>
                    <Fragment key={c.version}>
                        {c.instances && c.instances.length > 0 ?
                            <Fragment>
                                {this.groupByEnv(c.instances).map(env =>
                                    <Badge key={env.env}
                                           title={c.instances.filter(i => i.env === env.env).map(instance => instance.name).join(' ')}
                                           backgroundColor={backgroundColor(env.env)}
                                           color={color(env.env)}>
                                        {c.version}|
                                        {env.env} ({env.count})
                                    </Badge>
                                )}
                            </Fragment>
                            :
                            <Badge backgroundColor='#EEEEEE' color='gray'><i>{c.version}</i></Badge>
                        }
                    </Fragment>
                )}
                </span>
            </Fragment>
        );
    }
}

export { LibrariesApp }