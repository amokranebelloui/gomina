import React from "react";
import {Badge} from "../common/Badge";

class ConfCommited extends React.Component {
    render() {
        const display = !(this.props.commited == true);
        return (
            display &&
            <Badge title={'Config in not Committed'}
                   backgroundColor='red' color='white'>{this.props.commited == false ? 'chg' : '?'}</Badge>
        );
    }
}

export {ConfCommited}

