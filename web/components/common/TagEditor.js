// @flow
import * as React from "react"
import {Badge} from "./Badge";
import {Autocomplete} from "./AutoComplete";

type Props<T> = {
    tags?: ?Array<T>,
    sortAlphabetically?: ?boolean, // true

    suggestions?: Array<T>,
    idGetter?: T => ?string,
    labelGetter?: T => ?string,

    onTagAdd?: ?(T => void),
    onTagDelete?: ?(T => void)
}
type State<T> = {
    editionMode: boolean,
    additionMode: boolean,
    selected?: ?T,
    timeoutId?: ?TimeoutID,
    newTag?: ?T
}

class TagEditor<T> extends React.Component<Props<T>, State<T>> {
    constructor(props: Props<T>) {
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
    onSelected(value: ?T) {
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
    isSelected(value: T) {
        return this.state.selected === value
    }
    onNewTag() {
        //console.info("New tag");
        this.setState({additionMode: true});
    }
    onNewTagChanged(e: ?T) {
        this.setState({newTag: e});
        e && this.onAddTag();
    }
    onAddTag() {
        //console.info("Add tag", this.state.newTag);
        this.props.onTagAdd && this.state.newTag && this.props.onTagAdd(this.state.newTag)
    }
    onCancelAddTag() {
        //console.info("Cancel Add Tag", this.state.newTag);
        this.setState({additionMode: false});
    }
    onDeleteTag(value: T) {
        //console.info("Delete tag", value);
        this.props.onTagDelete && this.props.onTagDelete(value)
    }
    componentDidUpdate(prevProps: Props<T>, prevState: State<T>) {
        //console.info("update");
        //$FlowFixMe
        this.textInput.current && this.textInput.current.focus();
    }
    id(suggestion: ?T): string {
        //$FlowFixMe
        return (this.props.idGetter && suggestion ? this.props.idGetter(suggestion) : suggestion) || '';
    }
    label(suggestion: ?T): string {
        //$FlowFixMe
        return (this.props.labelGetter && suggestion ? this.props.labelGetter(suggestion) : suggestion) || '';
    }
    render() {
        let tags = this.props.tags || [];
        if (this.props.sortAlphabetically || true) {
            tags = tags.sort((i1, i2) => this.label(i2) > this.label(i1) ? -1 : 1);
        }

        let editor;
        if (this.props.suggestions) {
            editor = (
                <Autocomplete suggestions={this.props.suggestions}
                              idGetter={this.props.idGetter}
                              labelGetter={this.props.labelGetter}
                              onChange={e => this.onNewTagChanged(e)} />
            )
        }
        else {
            editor = (
                <input type="text" name="tag" placeholder="tag"
                    //$FlowFixMe
                       ref={this.textInput}
                       onChange={e => this.setState({newTag: e.target.value})}
                       onKeyPress={e => e.key === 'Enter' && this.onAddTag()}
                       onKeyDown={e => e.key === 'Escape' && this.onCancelAddTag()}
                />
            );
        }

        return (
            <div style={{display: 'inline-block'}}
                 onMouseEnter={() => this.onEnter()}
                 onMouseLeave={() => this.onLeave()}>
                {tags.map(t =>
                    <span key={this.id(t)} onMouseEnter={() => this.onSelected(t)}>
                    <Badge value={this.label(t)}>
                        {this.label(t)}
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
                        editor
                    }
                </Badge>
            </div>
        )
    }
}

export { TagEditor }