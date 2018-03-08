import React from "react";
import {Badge, CopyButton} from "../common/component-library";
import {ConfCommited, Expected, Host, Leader, Port, RedisLink, Versions} from "./instance-common";
import './instance.css'

class RedisInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        //let extra = instance.extra || {};
        let extra = instance || {}; // FIXME Extra
        const isSlave = extra.redisRole == 'SLAVE';
        //const isMaster = instance.extra.redisRole == 'MASTER';
        const persistenceModeColor = extra.redisMode == 'AOF' ? 'green' : extra.redisMode == 'RDB' ? 'lightgreen' : 'gray';
        /*<Status status={instance.status} />&nbsp;*/
        /* <Badge title={'Master in ServiceRegistry'} backgroundColor='#FFD21C' color='white'>***</Badge>*/
        return (
            <div className="instance">

                <div className="line">
                    <li><span style={{padding: "3px"}}><b>{instance.name}</b></span></li>
                    <li>
                        {extra.redisRW == 'rw' && <Badge backgroundColor='#969696' color='white' title="Read/Write">RW</Badge>}
                        {extra.redisRW == 'ro' && <Badge backgroundColor='#DBDBDB' color='white' title="Read Only">RO</Badge>}
                    </li>
                    <li>
                        <Badge title={'Role ' + extra.redisRole + ' ' + extra.redisSlaveCount + ' slaves'}>
                            {extra.redisRole} {extra.redisSlaveCount}
                        </Badge>
                    </li>
                    <li><Badge title={extra.redisClientCount + ' clients'}>{extra.redisClientCount}</Badge></li>

                    <li><ConfCommited commited={instance.confCommited} /></li>
                    <li><Expected expected={!instance.unexpected} /></li>

                    <li>
                        <Badge title={'Persistence Mode ' + extra.redisMode} backgroundColor={persistenceModeColor} color='white'>
                            {extra.redisMode}
                        </Badge>
                    </li>
                </div>

                <div className="line">
                     <li>
                        <span>
                            <span style={{display: 'block'}}><Host host={extra.redisHost} expected={instance.deployHost} />:<Port port={extra.redisPort} /></span>
                            <span style={{fontSize: 7, color: 'gray', lineHeight: '40%'}}>{extra.redisOffset}</span>
                        </span>
                    </li>
                    <li>
                        {extra.redisMaster && <Leader cluster={true} participating={true} leader={extra.redisMaster} />}
                        {isSlave && <RedisLink status={extra.redisMasterLink} since={extra.redisMasterLinkDownSince}/>}
                    </li>
                    {isSlave &&
                        <li>
                            <span>
                                <span style={{display: 'block'}}>{extra.redisMasterHost}:{extra.redisMasterPort}</span>
                                <span title="Offset (Diff)" style={{fontSize: 7, color: 'gray', lineHeight: '40%'}}>
                                    {isSlave && ('(' + extra.redisOffsetDiff + ')')}
                                </span>
                            </span>
                        </li>
                    }
                </div>

                <div className="line">
                    <li><span style={{userSelect: 'all', fontSize: 10}}>{instance.host}</span></li>
                    <li><span style={{userSelect: 'all', fontSize: 10}}>{instance.deployFolder}</span></li>
                    <li><CopyButton value={instance.deployFolder} /></li>
                    <li><Versions instance={instance} /></li>
                </div>
            </div>
        )
    }
}

export { RedisInstance }