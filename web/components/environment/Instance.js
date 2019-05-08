// @flow
import React, {Fragment} from "react";
import {Badge} from "../common/Badge";
import {ConfCommitted} from "../misc/ConfCommitted";
import {Expected} from "../misc/Expected";
import {BuildLink} from "../build/BuildLink";
import {Host} from "../misc/Host";
import './Instance.css'
import {Versions} from "./Versions";
import Link from "react-router-dom/es/Link";
import {ConfUpToDate} from "../misc/ConfUpToDate";
import type {VersionType} from "../common/version-utils";
import {instanceProperties, PropertyDisplayComponent} from "../../custom/CustomInstance";


const instancePropertiesMap = {};
instanceProperties.forEach(p =>
    instancePropertiesMap[p.property] = p.component
);

function propertiesFor(type: ?string): Array<PropertyDisplayComponent> {
    return type && instanceProperties.filter(p => p.type.includes(type)) || properties
}

type InstanceType = {
    id: string,
    env: string,
    name: ?string,
    //service: ?string,
    type: ?string,
    unexpected: boolean,
    unexpectedHost: boolean,
    cluster: boolean,
    participating: boolean,
    leader: boolean,
    pid: ?string,
    host: ?string,
    status: ?string,
    //startTime: ?number,
    //startDuration: ?number,
    componentId: ?string,
    deployHost: ?string,
    deployFolder: ?string,
    confCommitted: ?boolean,
    confUpToDate: ?boolean,
    confRevision: ?string,

    runningVersion?: ?VersionType,
    deployedVersion?: ?VersionType,
    releasedVersion?: ?VersionType,
    latestVersion?: ?VersionType,
    unreleasedChanges?: ?number,

    properties: {[string]: any}
}

type Props = {
    instance: InstanceType
}

class Instance extends React.Component<Props> {
    render() {
        const instance = this.props.instance;
        return (
            <Fragment>
            <td className="instance">
                <div className="section">
                    <li>
                        <b style={{fontSize: '12px'}}>{instance.name}</b>
                    </li>

                </div>
            </td>
            <td className="instance">
                <div className="section">
                <li>
                    <Badge>
                        {instance.pid &&
                        <Fragment>
                            <span style={{userSelect: 'all'}}>{instance.pid}</span>
                            <span style={{marginLeft: "1px", marginRight: "1px"}}>@</span>
                        </Fragment>
                        }
                        <Host host={instance.host} expected={instance.deployHost}/>
                    </Badge>
                </li>
                <li><Expected expected={!instance.unexpected} /></li>
            </div>
            </td>
            <td className="instance">
                <div className="section">
                <li>
                    <Versions running={instance.runningVersion} deployed={instance.deployedVersion}
                              released={instance.releasedVersion} latest={instance.latestVersion}
                              unreleasedChanges={instance.unreleasedChanges} />
                </li>
                <li><Link to={"/component/" + (instance.componentId||'')}>&rarr;</Link></li>
                <li><BuildLink url={instance.componentId}/></li> {/* // TODO Build URL */}
            </div>
            </td>
            <td className="instance">
                <div className="section">
                    {instance.deployFolder && <li><Badge><span style={{display: 'block', userSelect: 'all', fontSize: 10}}>{instance.deployFolder}</span></Badge></li>}

                    <li><Badge title={'Conf SVN revision'} backgroundColor='darkred' color='white'>{instance.confRevision || '?'}</Badge></li>
                    <li><ConfCommitted committed={instance.confCommitted}/></li>
                    <li><ConfUpToDate upToDate={instance.confUpToDate}/></li>
                </div>
            </td>
            <td className="instance">
                <div className="section">
                    {propertiesFor(instance.type).map(property =>
                        <li><InstanceProp property={property.property} comp={property.component} properties={instance.properties}/></li>
                    )}
                </div>
            </td>
            </Fragment>
        )
    }
}


function InstanceProperties(props: {properties: Array<[string, any]>}) {
    const properties = props.properties;
    return (
        <div>
        {properties &&
            Object.entries(properties || {}).sort((a,b) => (a[0] > b[0]) ? 1 : -1).map( p => {
                return <div><InstanceProperty  property={p[0]} value={p[1]} /></div>
            })
        }
        </div>
    )
}

function InstanceProperty(props: {property: string, value: any}) {
    const property = props.property;
    const value = props.value;
    const component = instancePropertiesMap[property] || (value => <span>{value && value.toString()}</span>);
    const p = value ? <b>{property}</b> : <span>{property}</span>;
    const c = component(value);
    return (
        <span style={{verticalAlign: 'middle'}}>
            <span>{p}</span> -> {c}
        </span>
    )
}

function InstanceProp(props: {property: string, comp: any => React.Component<any>, properties: {[string]: any}}) {
    const property = props.property;
    const value = (props.properties ||{})[property];
    //const component = instancePropertiesMap[property] || (value => <span>{value && value.toString()}</span>);
    const component = props.comp || (value => <span>{value && value.toString()}</span>);
    const c = component(value);
    return (
        value ?
        <span style={{verticalAlign: 'middle'}}>
            <span title={property}>{c}</span>
        </span>
        : null
    )
}

export { Instance, InstanceProperty, InstanceProperties }
export type { InstanceType }