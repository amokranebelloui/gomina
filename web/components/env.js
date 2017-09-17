
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
        return (
            <div>
                REDIS {instance.name}&nbsp;
                <Status status={instance.status} />&nbsp;
                <Version version={instance.version} revision={instance.revision}/>
                <br/>
                {instance.extra.redisRole} {instance.extra.redisRW}
                <br/>
                {instance.host} {instance.folder}
            </div>
        )
    }
}

class AppInstance extends React.Component {
    render() {
        const instance = this.props.instance;
        return (
            <div>
                {instance.name}&nbsp;
                <Status status={instance.status} />&nbsp;
                <Version version={instance.version} revision={instance.revision}/>
                <br/>
                {instance.host} {instance.folder}
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

