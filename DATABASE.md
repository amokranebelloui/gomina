Database structure    
Contexts, entities, schemas  

### Users (0)

### Components (1)
component:<componentId> (hash)
commit:<componentId>:<rev> (hset)
commits:<componentId> (zset) commit{timestamp}
versions:<componentId> (zset) version{releaseDate}

### Interaction (2)
- api:<source>:<componentId>:exposes:<functionName> (hash) name type
- api:<source>:<componentId>:uses:<functionName> (hash) name type usage

### Inventory (3)
- env:<envId> (hash) type description monitoring_url active
- service:<envId>:<svc> (hash) type mode active_count component
- services:<envId> (zset) svc{rank}
- instance:<envId>:<svc>:<instanceId> (hash) host folder


- `hmset env:UAT type TEST description "UAT env" monitoring_url "tcp://localhost@UAT"`
- `hmset service:UAT:referential type app mode ONE_ONLY active_count 1 component referential`
- `hmset service:UAT:order type app mode ONE_ONLY active_count 1 component order`
- `hmset service:UAT:basket type app mode ONE_ONLY active_count 1 component basket`

- `hmset instance:UAT:referential:referential1 host h1 folder /app/referential`
- `hmset instance:UAT:referential:referential2 host h2 folder /app/referential`
- `hmset instance:UAT:order:order1 host h1 folder /app/order`
- `hmset instance:UAT:order:order2 host h2 folder /app/order`
- `hmset instance:UAT:basket:basket1 host h1 folder /app/basket`
- `hmset instance:UAT:basket:basket2 host h2 folder /app/basket`

- `zadd services:UAT 1 basket 2 order 3 referential` 
- `zrange services:UAT 0 -1`
- `zadd services:UAT 2 basket 1 order` // Reordering
- `zrevrange services:UAT 0 0 withscores` // Highest score

- `zadd services:UAT 4 basket 1 order 2 referential` 

### Hosts (4)
- `hmset host:localhost type UAT username Test password_alias "@test"`
- `hmset host:jupiter-a data_center "europe-uk" group jupiter type PROD tags "trading, primary" username Test password_alias "@test" sudo "supergod"`
- `hmset host:jupiter-b data_center "europe-fr" group jupiter type PROD tags "trading, secondary" username Test password_alias "@test" sudo "supergod"`
- `hmset host:neptune data_center "us-east" type PROD username Test password_alias "@test" sudo "svc"`

### Work List (5)
- work:<workId> (hash) label type jira status people components


- `hmset work:poc label "Platform POC" type "Dev" jira "DEMO-540" people "amokrane.belloui" components "kernel,basket,order" status DEV`
- `hmset work:facilitation label "Facilitation" type "Dev" jira "DEMO-671" people "amokrane.belloui" components "facilitation, order" status DEV`
- `hmset work:refacto label "refacto 1" type "Dev" people "amokrane.belloui, john.doe" components "libbus" status COMPLETED`

### Events (6)
- event:<group>:<eventId> (hash) timestamp type message \[env instance component version\]

### Knowledge (7)
- knowledge:component:<componentId> (zset) user{knowledge}
- knowledge:user:<userId> (zset) component{knowledge}

 

