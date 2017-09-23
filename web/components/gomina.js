
import React from 'react';

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


function compareVersions (a, b) {
    var res = compareVersions2(a.replace("-SNAPSHOT", ""), b.replace("-SNAPSHOT", ""));
    return res != 0 ? res : a.includes("-SNAPSHOT") ? 0 : 1 - b.includes("-SNAPSHOT") ? 0 : 1;
}

function compareVersions2 (a, b) {
    var i, diff;
    var regExStrip0 = /(\.0+)+$/;
    var segmentsA = a.replace(regExStrip0, '').split('.');
    var segmentsB = b.replace(regExStrip0, '').split('.');
    var l = Math.min(segmentsA.length, segmentsB.length);

    for (i = 0; i < l; i++) {
        diff = parseInt(segmentsA[i], 10) - parseInt(segmentsB[i], 10);
        if (diff) {
            return diff;
        }
    }
    return segmentsA.length - segmentsB.length;
}

class Toggle extends React.Component {
    constructor(props) {
        super(props);
        //this.state = {isToggleOn: props.toggled};

        // This binding is necessary to make `this` work in the callback
        //this.toggle = this.toggle.bind(this);
    }

    toggle(e) {
        /*
         this.setState(prevState => ({
         isToggleOn: !prevState.isToggleOn
         }));
         */
        //this.setState({isToggleOn: e})
        if (this.props.onToggleChanged) {
            this.props.onToggleChanged(e)
        }
    }

    render() {
        return (
            <button onClick={e => this.toggle(!this.props.toggled)}>
                {this.props.toggled ? 'ON' : 'OFF'}
            </button>
        );
    }
}

class Badge extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        return (
            <span title={this.props.title}
                style={{padding: "3px", fontSize: '10px',
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
            <div style={{padding: '2px', margin: this.props.margin, display: this.props.block ? null : 'inline-block',
                backgroundColor: '#F8F8F8', border: '1px solid #E3E3E3', borderRadius: '5px'}}>
                {this.props.children}
            </div>
        )
    }
}

function Version(props) {
    const simplifiedVersion = props.version
        ? props.version.replace("-SNAPSHOT", "-S")
        : "unknown";

    const defaultStylingFunction = (version => isSnapshot(version) ? {color: 'white', backgroundColor: '#c30014'} : null);
    const stylingFunction = props.styling || defaultStylingFunction;
    const style = stylingFunction(props.version) || {color: 'black', backgroundColor: 'lightgray'}

    //const color = isSnapshot(props.version) ? "white" : "black";
    //const backgroundColor = isSnapshot(props.version) ? "#c30014" : "lightgray";
    const revision = props.revision ? props.revision : "*";
    return (
        <Badge color={style.color} backgroundColor={style.backgroundColor}>
            <span title={revision}>{simplifiedVersion}</span>
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

class CopyButton extends React.Component {
    copyToClipboard(text) {
        //event.originalTarget.parentNode.children[2].select()
        this.textEdit.select();
        let successful = document.execCommand('copy');
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

export {Toggle, Badge, Well, Status, CopyButton, Version, groupBy, isSnapshot, compareVersions}
