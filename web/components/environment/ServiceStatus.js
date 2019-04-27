// @flow
import * as React from "react";

type Props = {
    service: string,
    status?: ?string,
    reason?: ?string,
    text?: ?string,
    style?: ?any,
    onClick?: (e: SyntheticEvent<>) => void,
    onOrderChange?: (service: string, targetService: string) => void
}

class ServiceStatus extends React.Component<Props> {
    onDrag(e: SyntheticDragEvent<>) {
        e.dataTransfer.setData("svc", this.props.service);
    }
    allowDrop(e: SyntheticDragEvent<>) {
        e.preventDefault();
    }
    onDrop(e: SyntheticDragEvent<>) {
        e.preventDefault();
        var svc = e.dataTransfer.getData("svc");
        this.props.onOrderChange && this.props.onOrderChange(svc, this.props.service)
    }
    render() {
        const status = this.props.status;
        const reason = this.props.reason;
        const text = this.props.text;
        const backgroundColor =
            status == 'ERROR' ? '#840513' :
                status == 'LIVE' && reason !== '-' ? '#6ebf75' :
                    status == 'LIVE' ? '#00ad0e' :
                        status == 'LOADING' ? '#ffdc92' :
                            status == 'DOWN' ? '#FF0000' :
                                'lightgray';
        const opacity = 0.9;
        const style = Object.assign(this.props.style||{}, {
            backgroundColor: backgroundColor,
            color: 'white',
            cursor: 'pointer'
        });
        return (
            <td colSpan="2" className='status' style={style} onClick={e => this.props.onClick && this.props.onClick(e)}>
                <div style={{display: 'table', width: '100%', boxSizing: 'border-box', padding: '0px 2px'}}
                     draggable={true}
                     onDragStart={e => this.onDrag(e)}
                     onDragOver={e => this. allowDrop(e)}
                     onDrop={e => this.onDrop(e)}
                >

                    <div style={{display: 'table-row'}}>
                        <div style={{display: 'table-cell', textAlign: 'left', fontSize: '12px'}}><b>{status}</b></div>
                        <div style={{display: 'table-cell', textAlign: 'right'}}>
                            <span title={text} style={{fontSize: '7px', opacity: opacity}}>
                                {reason}
                            </span>
                        </div>
                    </div>
                </div>
            </td>
        );
    }
}

export { ServiceStatus }