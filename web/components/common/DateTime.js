// @flow
import React from 'react'

type Props = {
    date?: ?Date | ?number | ?string
}

function DateTime(props: Props) {
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

export {DateTime}