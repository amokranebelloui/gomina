// @flow
import * as React from "react"

type Props = {
    url?: ?string,
    id?: ?string,
    onChanged?: (url: ?string, id: ?string) => void;
    onEdited?: (url: ?string, id: ?string) => void;
    onEditionCancelled?: () => void;
}

type State = {
    url?: ?string,
    id?: ?string
}

class MonitoringEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            url: this.props.url,
            id: this.props.id
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    changeUrl(url: ?string) {
        this.setState({url: url});
        this.props.onChanged && this.props.onChanged(url, this.state.id)
    }
    changeId(id: ?string) {
        this.setState({id: id});
        this.props.onChanged && this.props.onChanged(this.state.url, id)
    }
    update() {
        this.props.onEdited && this.props.onEdited(this.state.url, this.state.id)
    }
    cancelEdition() {
        this.props.onEditionCancelled && this.props.onEditionCancelled()
    }
    componentDidMount() {
        //$FlowFixMe
        setTimeout(() => this.textInput.current && this.textInput.current.focus(), 0)
    }
    render() {
        return (
            <div style={{display: 'inline-block'}}>

                <input type="text" name="url" placeholder="URL"
                       value={this.state.url}
                       onChange={e => this.changeUrl(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       style={{width: '150px', fontSize: 9}}
                />
                <input type="text" name="id" placeholder="ID"
                       value={this.state.id}
                       onChange={e => this.changeId(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       style={{width: '150px', fontSize: 9}}
                />
            </div>
        );
    }
}

export { MonitoringEditor }