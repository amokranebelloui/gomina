

class Clock extends React.Component {
    constructor(props) {
        super(props);
        this.state = {date: new Date()};

    }
    componentDidMount() {
        console.info("mounted clock")
        this.tid = setInterval(() => this.tick(), 1000);
    }
    componentWillUnmount() {
        console.info("unmounted clock")
        clearInterval(this.tid);
    }
    tick() {
        console.info("tick")
        // setState to re-trigger a render
        this.setState({date: new Date()});
    }
    render() {
        return (
            <span>
                    <b>It's {this.state.date.toLocaleTimeString()}.</b>
                </span>
        );
    }
}

function Menu(props) {
    return (
        <div style={{display: "inline-block"}}>
            <a href="index.html">index</a>&nbsp;
            <a href="architecture.html">architecture</a>&nbsp;
            <a href="environments.html">envs</a>&nbsp;
            <a href="projects.html">projects</a>&nbsp;
            <a href="project.html">project</a>&nbsp;
            <a href="pipeline.html">pipeline</a>
        </div>
    );
}

class AppLayout extends React.Component {
    render() {
        return (
            <div>
                <span style={{float: 'right'}}><Clock /></span>
                <Menu />
                &nbsp;
                <br/>
                <h3>{this.props.title}</h3>
                {this.props.children}
            </div>
        );
    }
}

