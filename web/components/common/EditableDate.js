// @flow

import * as React from "react"
import {DateTime} from "./DateTime";
import DatePicker from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css";

type Props = {
    date: ?string,
    altText?: ?string,
    style?: ?any,
    onDateEdited?: ?string => void
}

type State = {
    edition: boolean,
    date?: ?string,
}

class EditableDate extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            edition: false,
            date: this.props.date
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    edit() {
        this.setState({
            edition: true,
            date: this.props.date
        });
        //$FlowFixMe
        setTimeout(() => this.textInput.current && this.textInput.current.focus(), 0)
    }
    update() {
        this.setState({edition: false});
        this.props.onDateEdited && this.state.date && this.props.onDateEdited(this.state.date)
    }
    cancelEdition() {
        this.setState({edition: false}) //, label: this.props.label
    }
    componentWillReceiveProps(nextProps: Props) {
        //console.info("EditableDateTime will receive props", this.props, nextProps);
    }
    render() {
        return (
            this.state.edition ? [
                <DatePicker
                    dateFormat="dd/MM/yyyy"
                    selected={this.state.date && new Date(this.state.date)}
                    //$FlowFixMe
                    ref={this.textInput}
                    onKeyPress={e => e.key === 'Enter' && this.update()}
                    onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                    onChange={date => this.setState({date: date.toISOString().substring(0, 10)})}
                    {...this.props}
                />,
                <button onClick={() => this.update()}>OK</button>,
                <button onClick={() => this.cancelEdition()}>Cancel</button>
                ]
            :
                <span onDoubleClick={() => this.edit()} {...this.props}>
                    {this.props.date
                        ? <DateTime date={this.props.date} />
                        : <span style={{opacity: .5, textDecoration: 'italic'}}>{this.props.altText || '?'}</span>
                    }
                </span>


        )
    }
}

export { EditableDate }