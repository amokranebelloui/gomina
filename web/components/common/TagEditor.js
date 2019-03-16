// @flow
import * as React from "react"
import {Badge} from "./Badge";
import {uniqCount} from "./utils";

type Props = {
    tags?: ?Array<string>,
    sortAlphabetically?: ?boolean, // true
    onTagAdd?: ?(string => void),
    onTagDelete?: ?(string => void)
}
type State = {
    editionMode: boolean,
    additionMode: boolean,
    selected?: ?string,
    timeoutId?: ?TimeoutID,
    newTag?: ?string
}

class TagEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            editionMode: false,
            additionMode: false,
            selected: null,
            timeoutId: null
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    onSelected(value: string) {
        this.setState({selected: value});
    }
    onLeave() {
        console.info("leave");
        const timeout = setTimeout(() => this.setState({
            editionMode: false,
            additionMode: false,
            selected: null
        }), 1000);
        this.setState({timeoutId: timeout})
    }
    onEnter() {
        //console.info("enter");
        this.setState({editionMode: true});
        this.state.timeoutId && clearTimeout(this.state.timeoutId)
    }
    isSelected(value: string) {
        return this.state.selected === value
    }
    onNewTag() {
        console.info("New tag");
        this.setState({additionMode: true});
    }
    onAddTag() {
        console.info("Add tag", this.state.newTag);
        this.props.onTagAdd && this.state.newTag && this.props.onTagAdd(this.state.newTag)
    }
    onCancelAddTag() {
        //console.info("Cancel Add Tag", this.state.newTag);
        this.setState({additionMode: false});
    }
    onDeleteTag(value: string) {
        console.info("Delete tag", value);
        this.props.onTagDelete && this.props.onTagDelete(value)
    }
    componentDidUpdate(prevProps: Props, prevState: State) {
        //console.info("update");
        //$FlowFixMe
        this.textInput.current && this.textInput.current.focus();
    }
    render() {
        let tags = this.props.tags || [];
        if (this.props.sortAlphabetically || true) {
            tags = tags.sort((i1, i2) => i2 > i1 ? -1 : 1);
        }

        return (
            <div style={{display: 'inline-block'}}
                 onMouseEnter={() => this.onEnter()}
                 onMouseLeave={() => this.onLeave()}>
                {tags.map(t =>
                    <span onMouseEnter={() => this.onSelected(t)}>
                    <Badge key={t} value={t}>
                        {t}
                        {this.isSelected(t) &&
                            <input type="button" value="-"
                                   style={{borderRadius: 20, marginLeft: 1, paddingLeft: 2, paddingRight: 2}}
                                   onClick={() => this.onDeleteTag(t)} />
                        }
                    </Badge>
                    </span>
                )}
                <Badge>
                    {!this.state.additionMode &&
                        <input type="button" value="+"
                               style={{borderRadius: 20, marginLeft: 2, marginRight: 2, opacity: this.state.editionMode ? 1: 0.3}}
                               onClick={e => this.onNewTag()}
                        />
                    }
                    {this.state.additionMode &&
                        <input type="text" name="tag" placeholder="tag"
                               //$FlowFixMe
                               ref={this.textInput}
                               onChange={e => this.setState({newTag: e.target.value})}
                               onKeyPress={e => e.key === 'Enter' && this.onAddTag()}
                               onKeyDown={e => e.key === 'Escape' && this.onCancelAddTag()}
                        />
                    }
                </Badge>
            </div>
        )
    }
}

export { TagEditor }