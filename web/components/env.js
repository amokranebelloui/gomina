
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

class CopyButton extends React.Component {
    copyToClipboard(text) {
        //event.originalTarget.parentNode.children[2].select()
        this.textEdit.select()
        var successful = document.execCommand('copy');
        console.log(successful, text, this.textEdit.value);
    }
    render() {
        return (
            <span style={{
                    display: 'inline-block',
                    backgroundColor: 'black', color: 'white',
                    padding: 1, fontSize: 9, borderRadius: 4,
                    cursor: 'pointer'}}
                  onClick={e => this.copyToClipboard(this.props.value)}>

                <span style={{padding: 1, verticalAlign: 'top', align: 'middle'}}>
                    &gt;<input type="text" readOnly
                                   ref={node => this.textEdit = node}
                                   value={this.props.value}
                                   style={{opacity: 0, backgroundColor: 'red', width: 1, height: 1, border: 0}} />
                    &gt;
                </span>
            </span>
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
        const highlightFunction = this.props.highlight || (instance => true);
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

class EnvironmentLogical extends React.Component {
    constructor(props) {
        super(props);
        this.state = {highlight: instance => true}
    }
    render() {
        const instances = this.props.instances;
        const services = groupBy(instances, 'service');
        const highlightAll = instance => true;
        const highlightDown = instance => instance.status == 'DOWN';
        const highlightSnapshot = instance => isSnapshot(instance.version);

        return (
            <div>
                <button onClick={e => this.setState({highlight: highlightAll})}>ALL</button>
                <button onClick={e => this.setState({highlight: highlightDown})}>DOWN</button>
                <button onClick={e => this.setState({highlight: highlightSnapshot})}>SNAPSHOTS</button>
                <button onClick={e => this.setState({highlight: instance => instance.host == '10.2.3.58'})}>10.2.3.58</button>
                <button onClick={e => this.setState({highlight: instance => instance.host == '10.2.3.56'})}>10.2.3.56</button>

                {Object.keys(services).map( service =>
                    <Service key={service} name={service} instances={services[service]} highlight={this.state.highlight} />
                )}
            </div>
        )
    }
}

