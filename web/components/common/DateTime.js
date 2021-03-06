// @flow
import React, {Fragment} from 'react'

type Props = {
    date?: ?Date | ?number | ?string,
    style?: ?any,
    displayTime?: ?boolean
}

function DateTime(props: Props) {
    if (props.date) {
        const date = new Date(props.date);
        const dataStyle = Object.assign({}, {lineHeight: '1px'}, props.style);
        const timeStyle = Object.assign({}, {color: 'gray', fontSize: '7px', lineHeight: '-10px'}, props.style);
        const displayTime = props.displayTime !== null && typeof props.displayTime !== 'undefined'
            ? props.displayTime : true;
        return (
            <span>
                <span style={dataStyle}>
                    {date.toLocaleDateString()}
                </span>
                {displayTime &&
                    <Fragment>
                        <span>&nbsp;</span>
                        <span style={timeStyle}>{date.toLocaleTimeString()}</span>
                    </Fragment>
                }
            </span>
        )
    }
    else {
        return null
    }
}

export {DateTime}