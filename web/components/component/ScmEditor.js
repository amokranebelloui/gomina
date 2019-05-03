// @flow

import * as React from "react"

type Props = {
    type?: ?string,
    url?: ?string,
    path?: ?string,
    hasMetadata?: ?boolean,
    onChanged?: (type: ?string, url: ?string, path: ?string, hasMetadata?: ?boolean) => void,
    onEdited?: (type: string, url: string, path: ?string, hasMetadata?: ?boolean) => void,
    onEditionCancelled?: () => void
}

type State = {
    type?: ?string,
    url?: ?string,
    path?: ?string,
    hasMetadata?: ?boolean
}

class ScmEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            type: this.props.type,
            url: this.props.url,
            path: this.props.path,
            hasMetadata: this.props.hasMetadata
        };
        //$FlowFixMe
        this.textInput = React.createRef();
    }
    changeType(type?: ?string) {
        this.setState({type: type});
        this.props.onChanged && this.props.onChanged(type, this.state.url, this.state.path, this.state.hasMetadata)
    }
    changeUrl(url?: ?string) {
        this.setState({url: url});
        this.props.onChanged && this.props.onChanged(this.state.type, url, this.state.path, this.state.hasMetadata)
    }
    changePath(path?: ?string) {
        this.setState({path: path});
        this.props.onChanged && this.props.onChanged(this.state.type, this.state.url, path, this.state.hasMetadata)
    }
    changeHasMetadata(hasMetadata?: ?boolean) {
        this.setState({hasMetadata: hasMetadata});
        this.props.onChanged && this.props.onChanged(this.state.type, this.state.url, this.state.path, hasMetadata)
    }
    update() {
        console.info("edited", this.state.url);
        this.props.onEdited && this.state.type && this.state.url && this.props.onEdited(this.state.type, this.state.url, this.state.path, this.state.hasMetadata)
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
            <div style={{display: 'inline-block'}}>
                <select name="type" value={this.state.type}
                        onChange={e => this.changeType(e.target.value)}
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
                       onChange={e => this.changeUrl(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       //$FlowFixMe
                       ref={this.textInput}
                       style={{width: '200px', fontSize: 9}}
                />
                <input type="text" name="path" placeholder="Path"
                       value={this.state.path}
                       onChange={e => this.changePath(e.target.value)}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
                       style={{width: '80px', fontSize: 9}}
                />&nbsp;
                <br/>
                Has Metadata &nbsp;
                <input type="checkbox" name="hasMetadata"
                       checked={this.state.hasMetadata}
                       onChange={e => this.changeHasMetadata(e.target.checked)}
                       onKeyPress={e => e.key === 'Enter' && this.update()}
                       onKeyDown={e => e.key === 'Escape' && this.cancelEdition()}
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