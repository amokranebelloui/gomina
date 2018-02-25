
import React from 'react';

function groupBy(list, property) {
    return list.reduce((result, obj) => {
        const value = obj[property];
        (result[value] = result[value] || []).push(obj);
        return result;
    }, {});
};

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
                border: this.props.border ? '1px solid ' + this.props.border : '',
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

class StatusWrapper extends React.Component {
    render() {
        const status = this.props.status;
        const color =
            status == 'LIVE' ? 'green' :
                status == 'LOADING' ? 'orange' :
                    status == 'DOWN' ? 'red' :
                        'lightgray';
        const background =
            status == 'LIVE' ? '#e8f4e9' :
                status == 'LOADING' ? '#f9edcf' :
                    status == 'DOWN' ? '#f8e8e3' :
                        '#ededed';
        //#F8F8F8
        return (
            <div style={{padding: '2px', margin: this.props.margin, display: this.props.block ? null : 'inline-block',
                backgroundColor: background, border: '3px solid ' + color, borderRadius: '5px'}}>
                {this.props.children}
            </div>
        )
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
                               value={this.props.value || ''}
                               style={{opacity: 0, backgroundColor: 'red', width: 1, height: 1, border: 0}} />
                    &gt;
                </span>
            </span>
        )
    }
}

export {Toggle, Badge, Well, Status, StatusWrapper, CopyButton, groupBy}
