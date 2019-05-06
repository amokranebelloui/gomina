import React from "react";
import {Badge} from "../common/Badge";

class ConfUpToDate extends React.Component {
    render() {
        const display = !(this.props.upToDate === true);
        const {text, desc} =
            this.props.upToDate === null ? {text: '?', desc: 'Unknown config status'} :
            this.props.upToDate === false ? {text: 'outdated', desc: 'Config in not up to date'} :
            {text: '', desc: ''};

        return (
            display && <Badge title={desc} backgroundColor='red' color='white'>{text}</Badge>
        );
    }
}

export {ConfUpToDate}

