import React from "react"
import PropTypes from "prop-types"
import {Badge} from "./Badge";

/**
 * List of tags, that can be sorted by importance, and limited
 */
class Tags extends React.Component {
    constructor(props) {
        super(props)
    }
    render() {
        const tags = this.props.tags.sort((i1, i2) => i2.count - i1.count);
        return (
            tags.map(t => <Badge key={t.value}>{t.value}({t.count})</Badge>)
        )
    }
}

Tags.propTypes = {
    tags: PropTypes.array.isRequired
};

export { Tags }