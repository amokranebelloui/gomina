// @flow
import * as React from "react";
import {Badge} from "../common/Badge";
import {ConfCommited} from "../misc/ConfCommited";
import {Expected} from "../misc/Expected";
import {BuildLink} from "../build/BuildLink";
import {Host} from "../misc/Host";
import {Leader} from "../misc/Leader";
import {RedisLink} from "../misc/RedisLink";
import {Port} from "../misc/Port";
import './Instance.css'
import {Versions} from "./Versions";
import {RedisOffset, RedisPersistence, RedisReadWrite} from "./RedisInstance";
import Link from "react-router-dom/es/Link";

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
    confCommited: ?boolean,
    //confUpToDate: ?boolean,
    confRevision: ?string,

    version: string, // FIXME Deprecated version field

    versions: Object,
    //sidecar: Object,
    properties: {[string]: any}
}

type Props = {
    instance: InstanceType
}

class Instance extends React.Component<Props> {
    render() {
        const instance = this.props.instance;
        return (
            <td className="instance">
                <div className="section">
                    <li>
                        <b style={{fontSize: '12px'}}>{instance.name}</b>
                    </li>
                    <li>
                        <Badge>
                            {instance.pid && <span>{instance.pid}@</span>}
                            <Host host={instance.host} expected={instance.deployHost}/>
                        </Badge>
                    </li>
                    <li><Versions versions={instance.versions} /></li>
                    <li><Expected expected={!instance.unexpected} /></li>
                </div>
                <div className="section">
                    {instance.deployFolder && <li><Badge><span style={{display: 'block', userSelect: 'all', fontSize: 10}}>{instance.deployFolder}</span></Badge></li>}

                    <li><Badge title={'Conf SVN revision'} backgroundColor='red' color='white'>{instance.confRevision}</Badge></li>
                    <li><ConfCommited commited={instance.confCommited}/></li>
                    <li><BuildLink url={instance.componentId}/></li> {/* // TODO Build URL */}
                </div>

                <div className="section">
                    {instance.type == 'app' && [
                        <li><InstanceProp property="jvm.jmx.port" properties={instance.properties}/></li>,
                        <li><InstanceProp property="xxx.bux.version" properties={instance.properties}/></li>,
                        <li><InstanceProp property="xxx.core.version" properties={instance.properties}/></li>,
                        <li><InstanceProp property="quickfix.persistence" properties={instance.properties}/></li>
                    ]}
                    {instance.type == 'redis' && [
                        <li><InstanceProp property="redis.port" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.role" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.slave.count" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.rw" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.persistence.mode" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.offset" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.client.count" properties={instance.properties}/></li>,

                        <li><InstanceProp property="redis.master" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.master.link" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.master.host" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.master.port" properties={instance.properties}/></li>,
                        <li><InstanceProp property="redis.master.offset.diff" properties={instance.properties}/></li>,
                    ]}
                </div>
            </td>
        )
    }
}

const map = {
    "redis.port": value => <Port port={value} />,
    "redis.master.host": value => <Host host={value} />,
    "redis.master.port": value => <Port port={value} />,
    "redis.master.link": value => value && <RedisLink status={value.status} since={value.downSince} />,
    "redis.offset": value => <RedisOffset offset={value} />,
    "redis.master.offset.diff": value => <RedisOffset offset={value} />,
    "redis.master": value => <Leader cluster={true} participating={true} leader={value} />,
    "redis.persistence.mode": value => <RedisPersistence redisMode={value} />,
    "redis.rw": value => <RedisReadWrite redisRW={value} />
};

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
    const component = map[property] || (value => <span>{value && value.toString()}</span>);
    const p = value ? <b>{property}</b> : <span>{property}</span>;
    const c = component(value);
    return (
        <span style={{verticalAlign: 'middle'}}>
            <span>{p}</span> -> {c}
        </span>
    )
}

function InstanceProp(props: {property: string, properties: {[string]: any}}) {
    const property = props.property;
    const value = (props.properties ||{})[property];
    const component = map[property] || (value => <span>{value && value.toString()}</span>);
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