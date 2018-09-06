import React from "react";
import {Badge} from "../../common/Badge";

class Expected extends React.Component {
    render() {
        const display = !(this.props.expected == true);
        return (
            display &&
            <Badge title={'Unexpected'}
                   backgroundColor='gray' color='white'>'???'</Badge>
        );
    }
}

export {Expected}
