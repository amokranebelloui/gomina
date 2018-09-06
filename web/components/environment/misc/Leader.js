import React from "react";
import {Badge} from "../../common/Badge";

function Leader(props) {
    const leader = props.leader == true;
    const participating = props.participating == true;
    const cluster = props.cluster == true;

    let title;
    if (cluster) {
        title = leader ? 'Elected Leader' : participating ? 'Contending for Leadership' : 'Standby instance'
    }
    else {
        title = leader ? 'Leader' : 'Not leader'
    }
    return (
        <Badge title={title}
               backgroundColor={leader ? '#ff9a22' : participating ? 'gray' : 'lightgray'}
               border={cluster ? '#ff8f14' : undefined}
               color='white'>{leader ? '***' : '-'}</Badge>
    )
}

export {Leader};
