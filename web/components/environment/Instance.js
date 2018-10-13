import React from "react";
import PropTypes from "prop-types"
import {Status} from "./Status";
import {Badge} from "../common/Badge";
import {ConfCommited} from "../misc/ConfCommited";
import {Expected} from "../misc/Expected";
import {BuildLink} from "../build/BuildLink";
import {CopyButton} from "../common/CopyButton";
import {Host} from "../misc/Host";
import {Leader} from "../misc/Leader";
import {RedisLink} from "../misc/RedisLink";
import {Port} from "../misc/Port";
import './Instance.css'
import {Versions} from "./Versions";
import {RedisPersistence, RedisReadWrite} from "./RedisInstance";

class Instance extends React.Component {
    render() {
        const instance = this.props.instance;
        const properties = Object.entries(instance.properties || {});
        const opacity = this.props.highlighted ? 1 : 0.06;
        
        return ([
            <Status key={'status' + instance.id} status={instance.status} leader={instance.leader}
                    participating={instance.participating} cluster={instance.cluster} style={{opacity: opacity}}/>,
            <div key={instance.id} className='instance-badge'
                 style={{display: 'table-cell', opacity: opacity}}>

                <div className="instance">
                    <div className="line">
                        <li><Badge><b>{instance.name} ({instance.type})</b></Badge></li>
                        <li>
                            <Badge>
                                {instance.pid && <span>{instance.pid}@</span>}
                                <Host host={instance.host} expected={instance.deployHost}/>
                            </Badge>
                        </li>
                        <li><Versions versions={instance.versions} /></li>
                        <li><Expected expected={!instance.unexpected} /></li>
                    </div>
                    <div className="line">
                        {instance.deployFolder && <li><Badge><span style={{userSelect: 'all', fontSize: 10}}>{instance.deployFolder}</span></Badge></li>}
                        {instance.deployFolder && <li><CopyButton value={instance.deployFolder}/></li>}

                        <li><Badge title={'Conf SVN revision'} backgroundColor='red' color='white'>{instance.confRevision}</Badge></li>
                        <li><ConfCommited commited={instance.confCommited}/></li>
                        <li><BuildLink url={instance.project}/></li> {/* // TODO Project Build URL */}
                    </div>

                    {instance.type == 'app' &&
                    <div className="line">
                        <li><b>@App</b></li>

                        <li><InstanceProp property="jvm.jmx.port" properties={instance.properties}/></li>
                        <li><InstanceProp property="xxx.bux.version" properties={instance.properties}/></li>
                        <li><InstanceProp property="xxx.core.version" properties={instance.properties}/></li>
                        <li><InstanceProp property="quickfix.persistence" properties={instance.properties}/></li>
                    </div>
                    }

                    {instance.type == 'redis' &&
                    <div className="line">
                        <li><b>@Redis</b></li>

                        <li><InstanceProp property="redis.rw" properties={instance.properties}/></li>
                        <li><InstanceProp property="redis.persistence.mode" properties={instance.properties}/></li>
                        <li><InstanceProp property="redis.slave.count" properties={instance.properties}/></li>
                        <li><InstanceProp property="redis.client.count" properties={instance.properties}/></li>
                        <li><InstanceProp property="redis.status" properties={instance.properties}/></li>
                        <li><InstanceProp property="redis.master" properties={instance.properties}/></li>
                        <li><InstanceProp property="redis.role" properties={instance.properties}/></li>
                        {/*
                        <li><InstanceProp property="redis.host" properties={instance.properties} /></li>
                        <li><InstanceProp property="redis.port" properties={instance.properties} /></li>
                        */}

                    </div>
                    }

                    {instance.type == 'redis' &&
                    <div className="line">
                        <li><b>@Redis Link</b></li>
                        {/* Not Yet Migrated */}
                        <li>
                            <span>
                                <span><Host host={instance.redisHost} expected={instance.deployHost} />:<Port port={instance.redisPort} /></span>
                                <span style={{marginLeft: '1px', fontSize: 7, color: 'gray', lineHeight: '40%'}}>{instance.redisOffset}</span>
                            </span>
                        </li>
                        {instance.redisRole == 'SLAVE' &&
                        <li>
                            <RedisLink status={instance.redisMasterLink} since={instance.redisMasterLinkDownSince}/>
                            <span><Host host={instance.redisMasterHost} />:<Port port={instance.redisMasterPort} /></span>
                            <span title="Offset (Diff)" style={{marginLeft: '1px', fontSize: 7, color: 'gray', lineHeight: '40%'}}>
                                ({instance.redisOffsetDiff})
                            </span>
                        </li>
                        }

                    </div>
                    }
                </div>
                
                <hr/>
                <div>
                    {instance.properties &&
                    properties.map( p => {
                        return <div><InstanceProperty  property={p[0]} value={p[1]} /></div>}
                    )}
                </div>
            </div>,
        ])
    }
}

Instance.propTypes = {
    "instance": PropTypes.object,
    "highlighted": PropTypes.bool
};

const map = {
    "redis.master": value => <Leader cluster={true} participating={true} leader={value} />,
    "redis.persistence.mode": value => <RedisPersistence redisMode={value} />,
    "redis.rw": value => <RedisReadWrite redisRW={value} />
};

function InstanceProperty(props) {
    const property = props.property;
    const value = props.value;
    const component = map[property] || (value => <span>{value}</span>);
    const p = value ? <b>{property}</b> : property;
    const c = component(value);
    return (
        <span style={{verticalAlign: 'middle'}}>
            <span>{p}</span> -> {c}
        </span>
    )
}

function InstanceProp(props) {
    const property = props.property;
    const value = (props.properties ||{})[property];
    const component = map[property] || (value => <span>{value}</span>);
    const c = component(value);
    return (
        value ?
        <span style={{verticalAlign: 'middle'}}>
            <span title={property}>{c}</span>
        </span>
        : null
    )
}

export { Instance }