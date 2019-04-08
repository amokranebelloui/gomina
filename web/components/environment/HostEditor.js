// @flow
import * as React from "react"
import type {HostDataType, HostType} from "./HostType";
import {TagEditor} from "../common/TagEditor";

type Props = {
    host: HostType,
    onChange?: (hostId: string, data: HostDataType) => void
}

type State = {
    dataCenter?: ?string,
    group?: ?string,
    type?: ?string,
    tags: Array<string>
}

class HostEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            dataCenter: this.props.host.dataCenter,
            group: this.props.host.group,
            type: this.props.host.type,
            tags: this.props.host.tags || [],
        };
    }
    changeDataCenter(value: string) {
        this.setState({dataCenter: value});
        this.notifyChange({dataCenter: value});
    }
    changeGroup(val: string) {
        this.setState({group: val});
        this.notifyChange({group: val});
    }
    changeType(val: ?string) {
        this.setState({type: val});
        this.notifyChange({type: val});
    }
    addTag(val: string) {
        const newTags = [...this.state.tags];
        newTags.push(val);
        this.setState({tags: newTags});
        this.notifyChange({tags: newTags});
    }
    deleteTag(val: string) {
        const newTags = [...this.state.tags].filter(i => i !== val);
        this.setState({tags: newTags});
        this.notifyChange({tags: newTags});
    }

    notifyChange(delta: any) {
        const fromState = {
            dataCenter: this.state.dataCenter,
            group: this.state.group,
            type: this.state.type,
            tags: this.state.tags
        };
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(this.props.host.host, data)
    }
    render() {
        return (
            <div>
                <input type="text" name="dataCenter" placeholder="DataCenter"
                       value={this.state.dataCenter}
                       onChange={e => this.changeDataCenter(e.target.value)} />
                <br/>
                <input type="text" name="group" placeholder="Group"
                       value={this.state.group}
                       onChange={e => this.changeGroup(e.target.value)} />
                <br/>
                <input type="text" name="type" placeholder="Type"
                       value={this.state.type}
                       onChange={e => this.changeType(e.target.value)} />
                <br/>
                <TagEditor tags={this.state.tags} onTagAdd={t => this.addTag(t)} onTagDelete={t => this.deleteTag(t)} />
                <br/>
            </div>
        );
    }
}

export { HostEditor }