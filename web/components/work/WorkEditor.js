// @flow
import * as React from "react"
import {TagEditor} from "../common/TagEditor";
import type {WorkDataType, WorkType} from "./WorkType";
import type {UserRefType} from "../misc/UserType";
import type {ComponentRefType} from "../component/ComponentType";
import DatePicker from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css";

type Props = {
    work?: WorkType,
    peopleRefs: Array<UserRefType>,
    componentsRefs: Array<ComponentRefType>,
    onChange?: (workId?: string, data: WorkDataType) => void
}

type State = {
    label: ?string,
    type: ?string,
    jira: ?string,
    people: Array<UserRefType>,
    components: Array<ComponentRefType>,
    dueDate: ?Date
}

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
            components: work && work.components || [],
            dueDate: work && work.dueDate
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
    addPeople(val: UserRefType) {
        if (!this.state.people.find(p => p.id === val.id)) {
            const newVal = [...this.state.people];
            newVal.push(val);
            this.setState({people: newVal});
            this.notifyChange({people: newVal.map(p => p.id)});
        }
    }
    deletePeople(val: UserRefType) {
        const newVal = [...this.state.people].filter(i => i !== val);
        this.setState({people: newVal});
        this.notifyChange({people: newVal.map(p => p.id)});
    }
    addComponent(val: ComponentRefType) {
        if (!this.state.components.find(c => c.id === val.id)) {
            const newVal = [...this.state.components];
            newVal.push(val);
            this.setState({components: newVal});
            this.notifyChange({components: newVal.map(c => c.id)});
        }
    }
    deleteComponent(val: ComponentRefType) {
        const newVal = [...this.state.components].filter(i => i !== val);
        this.setState({components: newVal});
        this.notifyChange({components: newVal.map(c => c.id)});
    }
    changeDueDate(val: Date) {
        val.setHours(23);
        val.setMinutes(59);
        this.setState({dueDate: val});
        this.notifyChange({dueDate: val});
    }

    notifyChange(delta: any) {
        const fromState: WorkDataType = {
            label: this.state.label,
            type: this.state.type,
            jira: this.state.jira,
            people: this.state.people.map(p => p.id),
            components: this.state.components.map(c => c.id),
            dueDate: this.state.dueDate
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
                <TagEditor tags={this.state.people}
                           suggestions={this.props.peopleRefs}
                           idProperty="id"
                           labelProperty="shortName"
                           onTagAdd={p => this.addPeople(p)}
                           onTagDelete={p => this.deletePeople(p)} />
                <br/>
                <b>Components </b>
                <TagEditor tags={this.state.components}
                           suggestions={this.props.componentsRefs}
                           idProperty="id"
                           labelProperty="label"
                           onTagAdd={c => this.addComponent(c)}
                           onTagDelete={c => this.deleteComponent(c)} />
                <br/>
                <DatePicker
                    dateFormat="dd/MM/yyyy"
                    selected={this.state.dueDate}
                    onChange={date => this.changeDueDate(date)}
                />
                <br/>
            </div>
        );
    }
}

export { WorkEditor }