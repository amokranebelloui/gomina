import React from "react";
import {Host} from "../components/misc/Host";
import {Port} from "../components/misc/Port";
import {RedisLink} from "../components/misc/RedisLink";
import {RedisOffset, RedisPersistence, RedisReadWrite} from "../components/environment/RedisInstance";
import {Leader} from "../components/misc/Leader";

type PropertyDisplayComponent = {
    property: string,
    type: Array<string>,
    component: any => React.Component
}

const instanceProperties: Array<PropertyDisplayComponent> = [
    {property: "xxx.bux.version", type: ["app"], component: value => <span>BUS:{value || '?'}</span>},
    {property: "xxx.core.version", type: ["app"], component: value => <span>CORE:{value || '?'}</span>},
    {property: "jvm.jmx.port", type: ["app"], component: value => <Port port={value} />},
    {property: "quickfix.persistence", type: ["app"]},

    {property: "redis.port", type: ["redis"], component: value => <Port port={value} />},
    {property: "redis.rw", type: ["redis"], component: value => <RedisReadWrite redisRW={value} />},
    {property: "redis.persistence.mode", type: ["redis"], component: value => <RedisPersistence redisMode={value} />},
    {property: "redis.offset", type: ["redis"], component: value => <RedisOffset offset={value} />},
    {property: "redis.role", type: ["redis"]},
    {property: "redis.slave.count", type: ["redis"]},
    {property: "redis.client.count", type: ["redis"]},
    {property: "redis.master", type: ["redis"], component: value => <Leader cluster={true} participating={true} leader={value} />},
    {property: "redis.master.link", type: ["redis"], component: value => value && <RedisLink status={value.status} since={value.downSince} />},
    {property: "redis.master.host", type: ["redis"], component: value => <Host host={value} />},
    {property: "redis.master.port", type: ["redis"], component: value => <Port port={value} />},
    {property: "redis.master.offset.diff", type: ["redis"], component: value => <RedisOffset offset={value} />},
];

export {instanceProperties}
export type {PropertyDisplayComponent}