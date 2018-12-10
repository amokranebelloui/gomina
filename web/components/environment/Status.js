// @flow
import * as React from "react";

function isRunning(status: ?string) {
    return status === 'LIVE' || status === 'LOADING';
}

type StatusWrapperType = {
    status: string,
    leader: boolean,
    participating: boolean,
    cluster: boolean,
    style?: ?any,
    children?: ?any
};

class StatusWrapper extends React.Component<StatusWrapperType> {
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
            <div style={{
                padding: '2px', margin: '2px', display: 'inline-block',
                backgroundColor: background, border: '3px solid ' + color, borderRadius: '5px'
            }}>
                {this.props.children}
            </div>
        )
    }
}

type StatusType = {
    status?: ?string,
    leader: boolean,
    participating: boolean,
    cluster: boolean,
    style?: ?any
};

class Status extends React.Component<StatusType> {
    render() {
        const status = this.props.status;
        const leader = this.props.leader;
        const participating = this.props.participating;
        const cluster = this.props.cluster;
        const backgroundColor =
            status == 'LIVE' && leader ? '#00ad0e' :
                status == 'LIVE' && participating ? '#69876f' :
                    status == 'LIVE' ? '#9aa89b' :
                        status == 'LOADING' && leader ? '#ff8600' :
                            status == 'LOADING' && participating ? '#ffdc92' :
                                status == 'LOADING' ? '#ebe5c5' :
                                    status == 'DOWN' && leader ? 'lightgray' :
                                        'lightgray';
        /*
         const badge = (
         <span style={{padding: "3px",
         color: 'white', backgroundColor: backgroundColor,
         fontSize: '9px',
         borderRadius: "5px", display: "inline-block"}}>
         {status}
         </span>
         )

         <span style={{float: 'left'}}>
                    {this.props.children}
                </span>
         */

        let role = {text: '-', title: 'No information', opacity: 0.3, fontStyle: null};
        if (isRunning(status)) {
            if (cluster) {
                if (leader) {
                    role = {text: 'LEADER', title: 'Elected Leader', opacity: 0.9, fontStyle: null}
                }
                else if (participating) {
                    role = {text: 'PARTICIP', title: 'Contending for Leadership', opacity: 0.7, fontStyle: null}
                }
                else {
                    role = {text: 'STANDBY', title: 'Standby instance', opacity: 0.3, fontStyle: null}
                }
            }
            else {
                if (leader) {
                    role = {text: 'LEADER', title: 'Leader', opacity: 0.9, fontStyle: 'italic'}
                }
                else {
                    role = {text: 'NONE', title: 'Not leader', opacity: 0.3, fontStyle: null}
                }
            }
        }
        return (
            <td className='status' style={Object.assign(this.props.style || {}, {
                backgroundColor: backgroundColor,
                color: 'white',
                cursor: 'pointer'
            })}>
                <div style={{display: 'table', width: '100%', boxSizing: 'border-box', padding: '1px'}}>

                    <div style={{display: 'table-row'}}>
                        <div style={{display: 'table-cell', textAlign: 'left', fontSize: '15px'}}><b>{status}</b></div>
                    
                        <div style={{display: 'table-cell', textAlign: 'right'}}>
                            <span title={role.title}
                                  style={{fontSize: '7px', fontStyle: role.fontStyle, opacity: role.opacity}}>
                                {role.text}
                            </span>
                        </div>
                    </div>
                </div>
            </td>

        );
    }
}

export {StatusWrapper, Status}
