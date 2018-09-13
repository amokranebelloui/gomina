import React from "react"
import PropTypes from "prop-types"
import {Badge} from "./Badge";

/**
 * Tag cloud, with the size or color representing the importance of a tag.
 * The items can be selectable
 */
class TagCloud extends React.Component {
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

TagCloud.propTypes = {
    tags: PropTypes.array.isRequired
};

export { TagCloud }