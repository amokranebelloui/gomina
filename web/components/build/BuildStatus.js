import React from 'react'
import PropTypes from 'prop-types'
import {Badge} from "../common/Badge";

function colorFor(status) {
    if (status) {
        switch (status) {
            case "SUCCESS": return {background: 'green', color: 'white'};
            default : return {background: 'gray', color: 'white'};
        }
    }
    else {
        return {background: 'lighgray', color: 'black'};
    }
    /*
    */
}

function BuildStatus(props) {
    let status = props.status || 'UNKNOWN';
    let color = colorFor(status);
    return (
        <Badge title='Build Status' backgroundColor={color.background} color={color.color}>
            {status}
        </Badge>
    )
}

BuildStatus.propTypes = {
    "status": PropTypes.string
};

export {BuildStatus}