// @flow
import * as React from "react"
import {TagEditor} from "../common/TagEditor";
import type {WorkDataType, WorkType} from "./WorkType";

type Props = {
    work?: WorkType,
    onChange?: (workId?: string, data: WorkDataType) => void
}

type State = WorkDataType

/*
label: ?string,
    type: ?string,
    jira: ?string,
    people: Array<string>,
    components: Array<string>
 */

class WorkEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        const work = this.props.work;
        this.state = {
            label: work && work.label,
            type: work && work.type,
            jira: work && work.jira,
            people: work && work.people || [],
            components: work && work.components || []
        };
    }
    changeLabel(value: string) {
        this.setState({label: value});
        this.notifyChange({label: value});
    }
    changeType(val: ?string) {
        this.setState({type: val});
        this.notifyChange({type: val});
    }
    changeJira(val: string) {
        this.setState({jira: val});
        this.notifyChange({jira: val});
    }
    addPeople(val: string) {
        const newVal = [...this.state.people];
        newVal.push(val);
        this.setState({people: newVal});
        this.notifyChange({people: newVal});
    }
    deletePeople(val: string) {
        const newVal = [...this.state.people].filter(i => i !== val);
        this.setState({people: newVal});
        this.notifyChange({people: newVal});
    }
    addComponent(val: string) {
        const newVal = [...this.state.components];
        newVal.push(val);
        this.setState({components: newVal});
        this.notifyChange({components: newVal});
    }
    deleteComponent(val: string) {
        const newVal = [...this.state.components].filter(i => i !== val);
        this.setState({components: newVal});
        this.notifyChange({components: newVal});
    }

    notifyChange(delta: any) {
        const fromState = {
            label: this.state.label,
            type: this.state.type,
            jira: this.state.jira,
            people: this.state.people,
            components: this.state.components
        };
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(this.props.work && this.props.work.id, data)
    }
    render() {
        return (
            <div>
                <b>Label </b>
                <input type="text" name="label" placeholder="Label"
                       value={this.state.label}
                       onChange={e => this.changeLabel(e.target.value)} />
                <br/>
                <b>Type </b>
                <input type="text" name="type" placeholder="Type"
                       value={this.state.type}
                       onChange={e => this.changeType(e.target.value)} />
                <br/>
                <b>Jira </b>
                <input type="text" name="jira" placeholder="Jira"
                       value={this.state.jira}
                       onChange={e => this.changeJira(e.target.value)} />
                <br/>
                <b>People </b>
                <TagEditor tags={this.state.people} onTagAdd={p => this.addPeople(p)} onTagDelete={p => this.deletePeople(p)} />
                <br/>
                <b>Components </b>
                <TagEditor tags={this.state.components} onTagAdd={c => this.addComponent(c)} onTagDelete={c => this.deleteComponent(c)} />
                <br/>
            </div>
        );
    }
}

export { WorkEditor }