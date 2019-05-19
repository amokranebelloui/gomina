// @flow
import React, {Fragment} from "react"
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import Link from "react-router-dom/es/Link";
import {compareVersions, compareVersionsReverse} from "../common/version-utils";
import type {ComponentRefType} from "../component/ComponentType";
import {Well} from "../common/Well";
import type {InstanceRefType} from "../environment/InstanceType";
import {Badge} from "../common/Badge";
import {groupingBy} from "../common/utils";
import type {EnvType} from "../environment/Environment";

type LibraryType = {
    artifactId: string,
    versions: Array<string>
}

type ComponentVersionType = {
    component: ComponentRefType,
    version: string,
    instances: Array<InstanceRefType>
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
    envs: Array<EnvType>,
    libraries: Array<LibraryType>,
    libraryId: ?string,
    library: Array<LibraryUsageType>
}

class LibrariesApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            search: '',
            envs: [],
            libraries: [],
            libraryId: this.props.match.params.id,
            library: []
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
        const libraries = (this.state.libraries || []).filter(l => this.matchesSearch(l));
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
                                <h3>Usage of '{this.state.libraryId}'</h3>
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