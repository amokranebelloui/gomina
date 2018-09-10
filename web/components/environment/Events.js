import React from "react";
import PropTypes from "prop-types";

class Events extends React.Component {
    constructor(props) {
        super(props)
    }

    render() {
        return (
            <div>
                {this.props.errors.map(error =>
                    <li key={error} style={{fontStyle: 'italic', color: 'gray'}}>
                        {error}
                    </li>
                )}
                <ul>
                    {this.props.events.map(event =>
                        <li key={event.timestamp + event.message}>
                            {new Date(event.timestamp).toLocaleString()}&nbsp;
                            {event.type}&nbsp;
                            {event.message}
                        </li>
                    )}
                </ul>
            </div>
        )
    }
}

Events.propTypes = {
    errors: PropTypes.array,
    events: PropTypes.array,
};

export { Events }