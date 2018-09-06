import React from "react";
import {Status} from "./Status";
import {AppInstance} from "./AppInstance";
import {RedisInstance} from "./RedisInstance";

class Instance extends React.Component {
    render() {
        const instance = this.props.instance;
        const opacity = this.props.highlighted ? 1 : 0.06;
        let comp;
        if (instance.type == 'redis') {
            comp = <RedisInstance instance={instance}/>
        }
        else {
            comp = <AppInstance instance={instance}/>
        }
        /*
        <span style={{verticalAlign: 'top', display: 'inline-block'}}>

                </span>
                <StatusWrapper status={instance.status}>
                            </StatusWrapper>


         */
        return ([
            <Status key={'status' + instance.id} status={instance.status} leader={instance.leader}
                    participating={instance.participating} cluster={instance.cluster} style={{opacity: opacity}}/>,
            <div key={instance.id} className='instance-badge'
                 style={{display: 'table-cell', opacity: opacity}}>{comp}</div>
        ])
    }
}

export { Instance }