import React from "react";
import {CopyButton} from "../common/utils";
import './Instance.css'
import {Badge} from "../common/Badge";

function RedisPersistence(props) {
    const persistenceModeColor = props.redisMode === 'AOF' ? 'green' : props.redisMode === 'RDB' ? 'lightgreen' : 'gray';
    return (
        <Badge title={'Persistence Mode ' + props.redisMode} backgroundColor={persistenceModeColor} color='white'>
            {props.redisMode}
        </Badge>
    )
}

function RedisReadWrite(props) {
    return (
        props.redisRW == 'rw'
            ? <Badge backgroundColor='#969696' color='white' title="Read/Write">RW</Badge>
            : props.redisRW == 'ro'
            ? <Badge backgroundColor='#DBDBDB' color='white' title="Read Only">RO</Badge>
            : null
    )
}

export { RedisPersistence, RedisReadWrite }