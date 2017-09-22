
class Instance extends React.Component {
    render() {
        const instance = this.props.instance;
        const opacity = this.props.highlighted ? 1 : 0.06;
        let comp;
        if (instance.type == 'redis') {
            comp = <RedisInstance instance={instance} />
        }
        else {
            comp = <AppInstance instance={instance} />
        }
        return (
            <Well>
                <div style={{display: "inline-block", verticalAlign: 'top', opacity:opacity}}>
                    {comp}
                </div>
            </Well>
        )
    }
}

class RedisInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        const isSlave = instance.extra.redisRole == 'SLAVE';
        const isMaster = instance.extra.redisRole == 'MASTER';
        const persistenceModeColor = instance.extra.redisMode == 'AOF' ? 'green' : instance.extra.redisMode == 'RDB' ? 'lightgreen' : 'gray';
        return (
            <div>
                <span>{instance.name}</span>&nbsp;
                {instance.extra.redisMaster && <Badge title={'Master in ServiceRegistry'} backgroundColor='#FFD21C' color='white'>***</Badge>}
                &nbsp;
                <Status status={instance.status} />&nbsp;
                {isSlave && (instance.extra.redisMasterInfo.link
                    ? <Badge title={'Link Up'} backgroundColor='#FCF6D4' color='white'>OK</Badge>
                    : <Badge title={'Link Down Since ' + instance.extra.redisMasterInfo.since} backgroundColor='#FF2929' color='white'>KO</Badge>)
                }
                &nbsp;
                {instance.extra.redisRW == 'rw' && <Badge backgroundColor='#969696' color='white' title="Read/Write">RW</Badge>}
                {instance.extra.redisRW == 'ro' && <Badge backgroundColor='#DBDBDB' color='white' title="Read Only">RO</Badge>}
                &nbsp;

                <span title={'Role ' + instance.extra.redisRole + ' ' + instance.extra.redisSlaveCount + ' slaves'}>
                    {instance.extra.redisRole} {instance.extra.redisSlaveCount}
                </span>
                &nbsp;

                {!instance.confCommited && <Badge title={'Config in not Committed'} backgroundColor='darkred' color='white'>chg</Badge>}

                <Badge title={'Persistence Mode ' + instance.extra.redisMode} backgroundColor={persistenceModeColor} color='white'>
                    {instance.extra.redisMode}
                </Badge>

                <span title={instance.extra.redisClientCount + ' clients'}>
                    {instance.extra.redisClientCount}
                </span>
                <br/>

                {instance.extra.redisInfo.host}:{instance.extra.redisInfo.port} -> {instance.extra.redisMasterInfo.host}:{instance.extra.redisMasterInfo.port}
                <span title="Offset (Diff)" style={{fontSize: 9, color: 'gray'}}>
                    {instance.extra.redisOffset} {isSlave && ('(' + instance.extra.redisOffsetDiff + ')')}
                </span>
                <br/>

                <CopyButton value={instance.folder} />
                <span style={{fontSize: 10}}>{instance.host} {instance.folder}</span>
                <Version version={instance.version} revision={instance.revision}/>
            </div>
        )
    }
}

class AppInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        //, alignItems: 'top', justifyContent: 'center'
        return (
            <div>
                <div>
                    <span>{instance.name}</span>
                    &nbsp;
                    <Status status={instance.status} />
                    &nbsp;
                    <Version version={instance.version} revision={instance.revision}/>
                    &nbsp;
                    {!instance.confCommited && <Badge title={'Config in not Committed'} backgroundColor='darkred' color='white'>chg</Badge>}

                    <BuildLink url={instance.project} /> {/* // TODO Project Build URL */}
                </div>
                <div style={{display: 'flex', fontSize: 10}}>
                    <span>{instance.host}</span>
                    &nbsp;
                    <CopyButton value={instance.folder} />
                    &nbsp;
                    <span>{instance.folder}</span>
                </div>
                <div>
                    <Badge>{instance.extra.quickfixPersistence}</Badge>
                    <Badge>{instance.extra.busVersion}</Badge>
                    <Badge>{instance.extra.coreVersion}</Badge>
                    <Badge>{instance.extra.jmx}</Badge>
                </div>
            </div>
        )
    }
}

class Service extends React.Component {
    render() {
        const instances = this.props.instances ? this.props.instances : [];
        const differentVersions = new Set(instances.map(instance => instance.version)).size > 1;
        const highlightFunction = this.props.highlightFunction || (instance => true);
        return (
            <div style={{padding: '2px'}}>
                <div style={{width: '140px', display: 'inline-block', verticalAlign: 'top', marginRight: '2px'}}>
                    <span><b>{this.props.name}</b></span>
                    <br/>
                    {differentVersions && <Badge backgroundColor="orange">versions?</Badge>}
                </div>
                {instances.map((instance) =>
                    <span key={instance.id} style={{marginLeft: '2px', marginRight: '2px'}}>
                            <Instance instance={instance} highlighted={highlightFunction(instance)}/>
                        </span>
                )}
            </div>
        )
    }
}

class Selection extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        return (
            <button style={{color: this.props.id == this.props.selected ? 'gray' : null}}
                    onClick={this.props.onSelectionChanged && this.props.onSelectionChanged}>{this.props.label}</button>
        )
    }
}

class EnvironmentLogical extends React.Component {
    constructor(props) {
        super(props);
        this.state = {id: 'all', highlight: instance => true}
    }
    changeSelected(id, highlightFunction) {
        console.log('this is:', id, highlightFunction);
        this.setState({id: id, highlight: highlightFunction})
    }
    render() {
        const instances = this.props.instances;
        const services = groupBy(instances, 'service');
        const iterable = instances.map(instance => instance.host);
        const hosts = Array.from(new Set(iterable)).sort();

        return (
            <div>

                <Selection id='all' label='ALL' selected={this.state.id}
                           onSelectionChanged={e => this.changeSelected('all', instance => true)} />
                <Selection id='loading' label='LOADING' selected={this.state.id}
                           onSelectionChanged={e => this.changeSelected('loading', instance => instance.status == 'LOADING')} />
                <Selection id='down' label='DOWN' selected={this.state.id}
                           onSelectionChanged={e => this.changeSelected('down', instance => instance.status == 'DOWN')} />
                <Selection id='running' label='RUNNING' selected={this.state.id}
                           onSelectionChanged={e => this.changeSelected('running', instance => instance.status == 'LIVE')} />

                <Selection id='snapshots' label='SNAPSHOTS' selected={this.state.id}
                           onSelectionChanged={e => this.changeSelected('snapshots', instance => isSnapshot(instance.version))} />
                <Selection id='released' label='RELEASED' selected={this.state.id}
                           onSelectionChanged={e => this.changeSelected('released', instance => !isSnapshot(instance.version))} />

                {hosts.map(host =>
                    <Selection key={host} id={host} label={host} selected={this.state.id}
                               onSelectionChanged={e => this.changeSelected(host, instance => instance.host == host)} />
                )}

                {Object.keys(services).map( service =>
                    <Service key={service} name={service} instances={services[service]} highlightFunction={this.state.highlight} />
                )}
            </div>
        )
    }
}

