import React from "react"
import PropTypes from "prop-types"
import {Badge} from "./Badge";

/**
 * Tag cloud, with the size or color representing the importance of a tag.
 * The items can be selectable
 */
class TagCloud extends React.Component {
    constructor(props) {
        super(props);
        this.state = {selected: []}
    }
    onSelected(value, multi) {
        let nextVal;
        if (multi) {
            if (this.state.selected.indexOf(value) !== -1) {
                nextVal = this.state.selected.filter(i => i !== value)
            }
            else {
                nextVal = this.state.selected.concat([value])
            }
        }
        else {
            if (this.state.selected.indexOf(value) !== -1) {
                nextVal = [];
            }
            else {
                nextVal = [value];
            }

        }
        this.setState({selected: nextVal});
        this.props.selectionChanged && this.props.selectionChanged(nextVal)
    }
    isSelected(value) {
        return this.state.selected.indexOf(value) > -1
    }
    render() {
        const tags = this.props.tags.sort((i1, i2) => i2.count - i1.count);
        return (
            tags.map(t =>
                <Badge key={t.value} value={t.value}
                       backgroundColor={this.isSelected(t.value) ? 'yellow' : null}
                       onSelected={(v, multi) => this.onSelected(v, multi)}>
                    {t.value}({t.count})
                </Badge>
            )
        )
    }
}

TagCloud.propTypes = {
    tags: PropTypes.array.isRequired,
    selectionChanged: PropTypes.func
};

export { TagCloud }