import React from 'react'
import PropTypes from 'prop-types'
import {Badge} from "../common/Badge";

function BuildNumber(props) {
    if (props.number) {
        return <Badge>#{props.number}</Badge>
    }
    return null
}

BuildNumber.propTypes = {
    number: PropTypes.number
};

export {BuildNumber}