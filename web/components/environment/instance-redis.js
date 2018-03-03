import React from "react";
import {Badge, CopyButton} from "../common/component-library";
import {ConfCommited, Expected, Host, Leader, Port, RedisLink, Versions} from "./instance-common";

class RedisInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        //let extra = instance.extra || {};
        let extra = instance || {}; // FIXME Extra
        const isSlave = extra.redisRole == 'SLAVE';
        //const isMaster = instance.extra.redisRole == 'MASTER';
        const persistenceModeColor = extra.redisMode == 'AOF' ? 'green' : extra.redisMode == 'RDB' ? 'lightgreen' : 'gray';
        /*<Status status={instance.status} />&nbsp;*/
        return (
            <div>
                <span style={{padding: "3px"}}><b>{instance.name}</b></span>
                &nbsp;

                {extra.redisRW == 'rw' && <Badge backgroundColor='#969696' color='white' title="Read/Write">RW</Badge>}
                {extra.redisRW == 'ro' && <Badge backgroundColor='#DBDBDB' color='white' title="Read Only">RO</Badge>}
                &nbsp;

                <Badge title={'Role ' + extra.redisRole + ' ' + extra.redisSlaveCount + ' slaves'}>
                    {extra.redisRole} {extra.redisSlaveCount}
                </Badge>
                &nbsp;
                <Badge title={extra.redisClientCount + ' clients'}>{extra.redisClientCount}</Badge>
                &nbsp;

                <ConfCommited commited={instance.confCommited} />
                <Expected expected={!instance.unexpected} />

                <Badge title={'Persistence Mode ' + extra.redisMode} backgroundColor={persistenceModeColor} color='white'>
                    {extra.redisMode}
                </Badge>

                <br/>

                <div style={{display: 'inline-block', marginTop: '3px'}}>
                    <span style={{display: 'inline-block', fontSize: 11, lineHeight: '10px', verticalAlign: 'middle'}}>
                        <span><Host host={extra.redisHost} expected={instance.deployHost} />:<Port port={extra.redisPort} /></span>
                        <br/>
                        <span style={{fontSize: 9, color: 'gray'}}>{extra.redisOffset}</span>
                    </span>
                    <span style={{display: 'inline-block', verticalAlign: 'middle', margin: '-4px 3px'}}>
                        {extra.redisMaster &&
                        /*
                        <Badge title={'Master in ServiceRegistry'}
                               backgroundColor='#FFD21C'
                               color='white'>***</Badge>*/
                        <Leader cluster={true} participating={true} leader={extra.redisMaster} />
                        }
                        {isSlave &&
                        <RedisLink status={extra.redisMasterLink} since={extra.redisMasterLinkDownSince}/>
                        }
                    </span>
                    {isSlave &&
                    <span style={{display: 'inline-block', fontSize: 11, lineHeight: '10px', verticalAlign: 'middle'}}>
                        <span>{extra.redisMasterHost}:{extra.redisMasterPort}</span><br/>
                        <span title="Offset (Diff)" style={{fontSize: 9, color: 'gray'}}>
                            {isSlave && ('(' + extra.redisOffsetDiff + ')')}
                        </span>
                    </span>
                    }
                </div>

                <br/>

                <span style={{userSelect: 'all', fontSize: 10}}>{instance.host}</span>&nbsp;
                <span style={{userSelect: 'all', fontSize: 10}}>{instance.deployFolder}</span>&nbsp;
                <CopyButton value={instance.deployFolder} />&nbsp;
                <Versions instance={instance} />
            </div>
        )
    }
}

export { RedisInstance }