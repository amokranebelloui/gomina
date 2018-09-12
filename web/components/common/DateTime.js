
import React from 'react'
import PropTypes from 'prop-types'

function DateTime(props) {
    if (props.date) {
        const date = new Date(props.date);
        return ([
            <span style={{lineHeight: '1px'}}>
                    {date.toLocaleDateString()}
                </span>,
            <span>&nbsp;</span>,
            <span style={{color: 'gray', fontSize: '7px', lineHeight: '-10px'}}>
                    {date.toLocaleTimeString()}
                </span>
        ])
    }
    else {
        return ("")
    }
}

DateTime.propTypes = {
    date: PropTypes.number.isRequired
};

export {DateTime}