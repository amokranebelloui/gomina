import React from "react";
import {Badge} from "../common/Badge";

class ConfCommitted extends React.Component {
    render() {
        const display = !(this.props.committed === true);

        const {text, desc} =
            this.props.committed === null ? {text: '?', desc: 'Unknown config status'} :
            this.props.committed === false ? {text: 'chg', desc: 'Config in not Committed'} :
                {text: '', desc: ''};

        return (
            display && <Badge title={desc} backgroundColor='red' color='white'>{text}</Badge>
        );
    }
}

export {ConfCommitted}

