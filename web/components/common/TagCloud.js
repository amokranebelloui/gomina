// @flow
import * as React from "react"
import {Badge} from "./Badge";
import {uniqCount} from "./utils";

type Props = {
    tags?: ?Array<string>,
    sortByCount?: ?boolean, // true
    displayCount?: ?boolean,
    sortAlphabetically?: ?boolean, // true
    selectionChanged?: ?Array<string> => void
}
type State = {
    selected: Array<string>
}

/**
 * Tag cloud, with the size or color representing the importance of a tag.
 * The items can be selectable
 */
class TagCloud extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {selected: []}
    }
    onSelected(value: string, multi: boolean) {
        let nextVal: Array<string>;
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
    isSelected(value: string) {
        return this.state.selected.indexOf(value) > -1
    }
    render() {
        let tags = uniqCount(this.props.tags);
        if (this.props.sortByCount || true) {
            tags = tags.sort((i1, i2) => {
                const diff = i2.count - i1.count;
                if (this.props.sortAlphabetically || true) {
                    return diff !== 0 ? diff : i2.value > i1.value ? -1 : 1
                }
                else {
                    return diff
                }
            });
        }
        else if (this.props.sortAlphabetically || true) {
            tags = tags.sort((i1, i2) => i2.value > i1.value ? -1 : 1);
        }

        return (
            tags.map(t =>
                <Badge key={t.value} value={t.value}
                       backgroundColor={this.isSelected(t.value) ? 'yellow' : null}
                       onSelected={(v, multi) => this.onSelected(v, multi)}>
                    {t.value}{(this.props.displayCount || false) && "(" + t.count + ")"}
                </Badge>
            )
        )
    }
}

export { TagCloud }