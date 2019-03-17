// @flow

import * as React from "react"

type Props = {
    type?: ?string,
    url?: ?string,
    path?: ?string,
    onEdited: (type: string, url: string, path: ?string) => void;
    onEditionCancelled: () => void;
}

type State = {
    type?: ?string,
    url?: ?string,
    path?: ?string
}

class ScmEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            type: this.props.type,
            url: this.props.url,
            path: this.props.path
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    update() {
        console.info("edited", this.state.url);
        this.props.onEdited && this.state.type && this.state.url && this.props.onEdited(this.state.type, this.state.url, this.state.path)
    }
    cancelEdition() {
        console.info("cancel edition");
        this.props.onEditionCancelled && this.props.onEditionCancelled()
    }
    componentDidMount() {
        //$FlowFixMe
        setTimeout(() => this.textInput.current && this.textInput.current.focus(), 0)
    }
    render() {
        //<input type="text" name="scmType" placeholder="Type" onChange={e => this.setState({type: e.target.value})} />
        return (
            <div>
                <select name="type" value={this.state.type}
                        onChange={e => this.setState({type: e.target.value})}
                        style={{width: '50px', fontSize: 9}}>
                    <option value=""></option>
                    <option value="git">GIT</option>
                    <option value="svn">SVN</option>
                    <option value="mercurial">Mercurial</option>
                    <option value="cvs">CVS</option>
                    <option value="dummy">Dummy</option>
                </select>
                <input type="text" name="url" placeholder="Repo URL"
                       value={this.state.url}
                       onChange={e => this.setState({url: e.target.value})}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       //$FlowFixMe
                       ref={this.textInput}
                       style={{width: '200px', fontSize: 9}}
                />
                <input type="text" name="path" placeholder="Path"
                       value={this.state.path}
                       onChange={e => this.setState({path: e.target.value})}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       style={{width: '80px', fontSize: 9}}
                />
                <br/>
                {/*
                <span>{this.state.type}://{this.state.url}{this.state.path}</span>
                */}
            </div>
        )
    }
}

export { ScmEditor }