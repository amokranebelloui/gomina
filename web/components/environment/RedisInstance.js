// @flow
import * as React from "react";
import './Instance.css'
import {Badge} from "../common/Badge";

type RedisPersistenceType = {
    redisMode?: string
}

function RedisPersistence(props: RedisPersistenceType) {
    const persistenceModeColor = props.redisMode === 'AOF' ? 'green' : props.redisMode === 'RDB' ? 'lightgreen' : 'gray';
    return (
        <Badge title={'Persistence Mode ' + (props.redisMode||'')} backgroundColor={persistenceModeColor} color='white'>
            {props.redisMode}
        </Badge>
    )
}

type RedisReadWriteType = {
    redisRW?: string
}

function RedisReadWrite(props:RedisReadWriteType) {
    return (
        props.redisRW == 'rw'
            ? <Badge backgroundColor='#969696' color='white' title="Read/Write">RW</Badge>
            : props.redisRW == 'ro'
            ? <Badge backgroundColor='#DBDBDB' color='white' title="Read Only">RO</Badge>
            : null
    )
}

type RedisOffsetType = {
    offset?: string
}

function RedisOffset(props: RedisOffsetType) {
    return (
        <span style={{fontSize: 7, color: 'gray', lineHeight: '40%'}}>{props.offset}</span>
    )
}

export { RedisPersistence, RedisReadWrite, RedisOffset }