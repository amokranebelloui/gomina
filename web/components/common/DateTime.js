
import React from 'react'
import PropTypes from 'prop-types'

function DateTime(props) {
    if (props.date) {
        const date = new Date(props.date);
        return ([
            <span key="date" style={{lineHeight: '1px'}}>
                    {date.toLocaleDateString()}
                </span>,
            <span key="sep">&nbsp;</span>,
            <span key="time" style={{color: 'gray', fontSize: '7px', lineHeight: '-10px'}}>
                    {date.toLocaleTimeString()}
                </span>
        ])
    }
    else {
        return null
    }
}

DateTime.propTypes = {
    date: PropTypes.number
};

export {DateTime}