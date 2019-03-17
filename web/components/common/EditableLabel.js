// @flow

import * as React from "react"

type Props = {
    label: ?string,
    style?: ?any,
    onLabelEdited?: string => void
}

type State = {
    edition: boolean,
    label?: ?string
}

class EditableLabel extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            edition: false,
            label: this.props.label
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    editLabel() {
        this.setState({
            edition: true,
            label: this.props.label
        });
        //$FlowFixMe
        setTimeout(() => this.textInput.current && this.textInput.current.focus(), 0)
    }
    update() {
        //console.info("update label", this.state.label);
        this.setState({edition: false});
        this.props.onLabelEdited && this.state.label && this.props.onLabelEdited(this.state.label)
    }
    cancelEdition() {
        this.setState({edition: false}) //, label: this.props.label
    }
    componentWillReceiveProps(nextProps: Props) {
        console.info("EditableLabel will receive props", this.props, nextProps);
    }
    render() {
        return (
            this.state.edition ?
                <input type="text" name="label" placeholder=""
                       value={this.state.label}
                       //$FlowFixMe
                       ref={this.textInput}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       onChange={e => this.setState({label: e.target.value})}
                       {...this.props} />
            :
                <span onDoubleClick={() => this.editLabel()} {...this.props}>
                    {this.props.label}
                </span>


        )
    }
}

export { EditableLabel }