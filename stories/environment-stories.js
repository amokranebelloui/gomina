import React from 'react';
import {storiesOf} from '@storybook/react';
import {Service} from "../web/components/environment/Service";
import {ServiceStatus} from "../web/components/environment/ServiceStatus";
import {Instance} from "../web/components/environment/Instance";
import {Status} from "../web/components/environment/Status";
import {FixStatus} from "../web/components/misc/FixStatus";

var service = {
    svc: "checkout",
    type: "app",
    mode: "ONE_ONLY", // ONE_ONLY, LEADERSHIP, LOAD_BALANCING, OFFLINE,
    activeCount: 1,
    componentId: "basket"
};

//status LIVE LOADING, leader, version, unexpected, confCommitted, confRevision
const co1 = {id: "co1", status: "LIVE", leader: true, version: "2.3.6", confCommitted: true, confRevision: 12};
const co2 = {id: "co2", status: "DOWN", leader: false, version: "2.3.6", confCommitted: true, confRevision: 12};
const co3 = {id: "co3", status: "DOWN", leader: false, version: "2.3.5", confCommitted: true, confRevision: 11}; // Wrong version/config
const co4 = {id: "co4", status: "LIVE", leader: true, version: "2.3.6", confCommitted: true, confRevision: 11, unexpected: true}; // Unexpected Leader
const co5 = {id: "co5", status: "LIVE", leader: false, version: "2.3.6", confCommitted: true, confRevision: 12}; // Running backup

storiesOf('Service', module)
    .add('Service Status', () => {
        return ([
            <ServiceStatus status="LIVE" reason="-" />,
            <ServiceStatus status="LIVE" reason="because it's live" text="Is live" />,
            <ServiceStatus status="DOWN" reason="down" />,
            <ServiceStatus status="ERROR" reason="error" />,
            <ServiceStatus status="STYLED" reason="error" style={{border: '1px solid purple'}} />
        ])
    })
    .add('Only One', () => {
        return (
            <div style={{width: '200px'}}>
                <Service service={service} instances={[co1, co2]} />
                <Service service={service} instances={[co1, co3]} />
                <Service service={service} instances={[co1, co2, co4]} />
                <Service service={service} instances={[co1]} highlightFunction={i => false} />
            </div>
        )
    })
    .add('Leadership', () => {
        const serviceL = Object.assign({}, service, {mode: 'LEADERSHIP'});
        return (
            <div style={{width: '200px'}}>
                <Service service={serviceL} instances={[co1, co5]} />
                <Service service={serviceL} instances={[co1, co2]} />
                <Service service={serviceL} instances={[co1, co2, co4]} />
                <Service service={serviceL} instances={[co2, co3]} />
            </div>
        )
    });


storiesOf('Instances', module)
    .add('FIX Status', () => {
        return ([
            <FixStatus session="FIX.4.4:CLIENT:BROKER" connected={true} />,
            <FixStatus session="FIX.4.4:CLIENT:BROKER" connected={false} />
        ])
    })
    .add('Status', () => {
        return ([
            <Status status="LIVE" leader={true} cluster={true} participating={true} />,
            <Status status="LIVE" leader={true} cluster={true} participating={false} />,
            <Status status="DOWN" />,

        ])
    })
    .add('Instance', () => {
        const app = {
            id: "UAT-co1", type: 'app', name: "checkout1", componentId: 'basket',
            status: 'LIVE', leader: true, participating: true, cluster: true,
            pid: '45356', host:"jupiter1", deployHost: "saturn1",
            /*version: '2.3.4', revision: 12304, deployVersion: '2.3.5', deployRevision: 12456,*/
            versions: {
                running: {version: '2.3.4', revision: 12304},
                deployed: {version: '2.3.5', revision: 12456},
                released: {version: '2.3.6', revision: 12512},
                latest: {version: '2.3.7-SNAPSHOT', revision: 12521}
            },
            deployFolder: "/home/cart/services/co1", confRevision: 12360, confCommitted: false, unexpected: true,

            quickfixPersistence: 'REDIS', busVersion: '5.0.0', coreVersion: "1.4.5", jmx: 7012
        };
        const redis = {
            id: "UAT-db1", type: 'redis', name: "database1",
            status: 'LIVE', leader: true, participating: true, cluster: true,
            pid: '45356', host:"saturn1", deployHost: "saturn1",
            versions: {running: {version: '2.3.4', revision: 12304}, deployed: {version: '2.3.5', revision: 12456}},
            deployFolder: "/home/cart/data/db1", confRevision: 12360, confCommitted: false, unexpected: true,

            redisHost: "saturn1", redisPort: 7021, redisOffset: 454,
            redisMaster: false, redisMasterLink: true, redisMasterLinkDownSince: 1,
            redisMasterHost: "jupiter1", redisMasterPort: 7021, redisOffsetDiff: -3,
            redisMode: 'AOF', redisRole: 'SLAVE', redisRW: 'RO', residRole: 'slave', redisSlaveCount: 1, redisClientCount: 14
        };
        return ([
            <Instance instance={app} highlighted={true} />,
            <Instance instance={redis} highlighted={true} />,
            <Instance instance={redis} highlighted={false} />

        ])
    });
