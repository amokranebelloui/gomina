import React from "react";
import {Host} from "../components/misc/Host";
import {Port} from "../components/misc/Port";
import {RedisLink} from "../components/misc/RedisLink";
import {RedisOffset, RedisPersistence, RedisReadWrite} from "../components/environment/RedisInstance";
import {Leader} from "../components/misc/Leader";
import type {PropertyDisplayComponent} from "../components/environment/Instance";
import {FixStatus} from "../components/misc/FixStatus";

const instanceProperties: Array<PropertyDisplayComponent> = [
    {property: "xxx.bux.version", type: ["app"], component: (p, v) => <span>BUS:{v || '?'}</span>},
    {property: "xxx.core.version", type: ["app"], component: (p, v) => <span>CORE:{v || '?'}</span>},
    {property: "jvm.jmx.port", type: ["app"], component: (p, v) => <Port port={v} />},
    {property: "quickfix.persistence", type: ["app"]},
    {property: /fix\.session\..*/, type: [], component: (p, v) => <FixStatus session={p.substring(12)} connected={v} />},

    {property: "redis.port", type: ["redis"], component: (p, v) => <Port port={v} />},
    {property: "redis.rw", type: ["redis"], component: (p, v) => <RedisReadWrite redisRW={v} />},
    {property: "redis.persistence.mode", type: ["redis"], component: (p, v) => <RedisPersistence redisMode={v} />},
    {property: "redis.offset", type: ["redis"], component: (p, v) => <RedisOffset offset={v} />},
    {property: "redis.role", type: ["redis"]},
    {property: "redis.slave.count", type: ["redis"]},
    {property: "redis.client.count", type: ["redis"]},
    {property: "redis.master", type: ["redis"], component: (p, v) => <Leader cluster={true} participating={true} leader={v} />},
    {property: "redis.master.link", type: ["redis"], component: (p, v) => v && <RedisLink status={v.status} since={v.downSince} />},
    {property: "redis.master.host", type: ["redis"], component: (p, v) => <Host host={v} />},
    {property: "redis.master.port", type: ["redis"], component: (p, v) => <Port port={v} />},
    {property: "redis.master.offset.diff", type: ["redis"], component: (p, v) => <RedisOffset offset={v} />},
];

export {instanceProperties}
