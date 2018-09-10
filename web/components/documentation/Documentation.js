import React from "react";
import PropTypes from "prop-types";

class Documentation extends React.Component {
    componentDidMount() {
        this.content.innerHTML = this.props.doc;
    }
    componentWillReceiveProps(nextProps) { // Only on changes
        this.content.innerHTML = nextProps.doc;
    }

    render() {
        return (
            <div>
                <div ref={(ref) => {
                    this.content = ref;
                }}></div>
            </div>
        )
    }
}

Documentation.propTypes = {
    doc: PropTypes.string.isRequired
};

export {Documentation}
