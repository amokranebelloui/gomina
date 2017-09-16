function groupBy(list, property) {
    return list.reduce((result, obj) => {
        const value = obj[property];
        (result[value] = result[value] || []).push(obj);
        return result;
    }, {});
};

function isSnapshot(version) {
    return version ? version.includes("-SNAPSHOT") : false;
}

function Menu(props) {
    return (
        <div style={{display: "inline-block"}}>
            <a href="index.html">index</a>&nbsp;
            <a href="environments.html">envs</a>&nbsp;
            <a href="pipeline.html">pipeline</a>
        </div>
    );
}

class Badge extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        return (
            <span style={{padding: "3px", fontSize: '10px',
                color: this.props.color, backgroundColor: this.props.backgroundColor,
                borderRadius: "5px", display: "inline-block"}}>
                    {this.props.children}
                </span>
        )
    }
}

class Well extends React.Component {
    render() {
        return (
            <div style={{padding: '2px', display: 'inline-block',
                backgroundColor: '#F8F8F8', border: '1px solid #E3E3E3', borderRadius: '5px'}}>
                {this.props.children}
            </div>
        )
    }
}

function Version(props) {
    const version = props.version
        ? props.version.replace("-SNAPSHOT", "-S")
        : "unknown";
    const color = isSnapshot(props.version) ? "white" : "black";
    const backgroundColor = isSnapshot(props.version) ? "red" : "lightgray";
    const revision = props.revision ? props.revision : "*";
    return (
        <Badge color={color} backgroundColor={backgroundColor}>
            <span title={revision}>{version}</span>
            &nbsp;
            {isSnapshot && <span style={{fontSize: "9px"}}>({revision})</span>}
        </Badge>
    )
}

class Status extends React.Component {
    render() {
        const status = this.props.status;
        const backgroundColor =
            status == 'LIVE' ? 'green' :
                status == 'LOADING' ? 'orange' :
                    status == 'DOWN' ? 'gray' :
                        'red';
        const badge = <Badge color="white" backgroundColor={backgroundColor}>{status}</Badge>
        /*
         const badge = (
         <span style={{padding: "3px",
         color: 'white', backgroundColor: backgroundColor,
         fontSize: '9px',
         borderRadius: "5px", display: "inline-block"}}>
         {status}
         </span>
         )
         */
        return badge;
    }
}