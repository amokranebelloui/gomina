import React from 'react'
import PropTypes from 'prop-types'
import {Badge} from "../common/Badge";

function colorFor(status) {
    if (status) {
        switch (status) {
            case "SUCCESS": return {background: 'green', color: 'white'};
            case "FAILURE": return {background: '#cb0000', color: 'white'};
            case "BUILDING": return {background: '#3985bd', color: 'white'};
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
        <Badge title='Build Status' style={{width: '55px', textAlign: 'center'}} backgroundColor={color.background} color={color.color}>
            {status}
        </Badge>
    )
}

BuildStatus.propTypes = {
    "status": PropTypes.string
};

export {BuildStatus}