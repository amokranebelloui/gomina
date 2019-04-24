// @flow
import * as React from "react"
import {TagEditor} from "../common/TagEditor";
import type {IssueRefType, WorkDataType, WorkType} from "./WorkType";
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
    issues: Array<string>,
    people: Array<UserRefType>,
    components: Array<ComponentRefType>,
    dueDate: ?string
}

class WorkEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        const work = this.props.work;
        this.state = {
            label: work && work.label,
            type: work && work.type,
            issues: work && work.issues.map(i => i.issue) || [],
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

    addIssue(val: string) {
        if (!this.state.issues.find(p => p === val)) {
            const newVal = [...this.state.issues];
            newVal.push(val);
            this.setState({issues: newVal});
            this.notifyChange({issues: newVal});
        }
    }
    deleteIssue(val: string) {
        const newVal = [...this.state.issues].filter(i => i !== val);
        this.setState({issues: newVal});
        this.notifyChange({issues: newVal});
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
        const newVal = val.toISOString().substring(0, 10);
        this.setState({dueDate: newVal});
        this.notifyChange({dueDate: newVal});
    }

    notifyChange(delta: any) {
        const fromState: WorkDataType = {
            label: this.state.label,
            type: this.state.type,
            issues: this.state.issues,
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
                <b>Issues </b>
                <TagEditor tags={this.state.issues}
                           onTagAdd={p => this.addIssue(p)}
                           onTagDelete={p => this.deleteIssue(p)} />

                <br/>
                <b>People </b>
                <TagEditor tags={this.state.people}
                           suggestions={this.props.peopleRefs}
                           idGetter={s => s.id} labelGetter={s => s.shortName}
                           onTagAdd={p => this.addPeople(p)}
                           onTagDelete={p => this.deletePeople(p)} />
                <br/>
                <b>Components </b>
                <TagEditor tags={this.state.components}
                           suggestions={this.props.componentsRefs}
                           idGetter={s => s.id} labelGetter={s => s.label}
                           onTagAdd={c => this.addComponent(c)}
                           onTagDelete={c => this.deleteComponent(c)} />
                <br/>
                <DatePicker
                    dateFormat="dd/MM/yyyy"
                    selected={this.state.dueDate && new Date(this.state.dueDate)}
                    onChange={date => this.changeDueDate(date)}
                />
                <br/>
            </div>
        );
    }
}

export { WorkEditor }