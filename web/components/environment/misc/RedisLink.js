import React from "react";
import {Badge} from "../../common/Badge";

class RedisLink extends React.Component {
    render() {
        const status = this.props.status;
        const color = status == true ? 'green' : status == false ? 'red' : null;
        const title = status == false ? 'Link down since ' + this.props.since : 'Link Up';
        return (
            <Badge title={title} backgroundColor={color} color='white'>
                <b>&rarr;</b>
            </Badge>
        );
    }
}

export {RedisLink}
