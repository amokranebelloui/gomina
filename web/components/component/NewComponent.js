// @flow
import * as React from "react"

type NewComponentType = {
    id: string,
    label?: ?string,
    type?: ?string,
    systems: Array<string>,
    languages: Array<string>,
    tags: Array<string>,
    scmType?: ?string,
    scmUrl?: ?string,
    scmPath?: ?string,
    maven?: ?string,
    sonarServer?: ?string,
    jenkinsServer?: ?string,
    jenkinsJob?: ?string
}

type Props = {
    processing: boolean,
    error?: ?string,
    onAdd: NewComponentType => void,
    onCancel: () => void
}

class NewComponent extends React.Component<Props, NewComponentType> {
    constructor(props: Props) {
        super(props);
        this.state = {
            id: '',
            systems: [],
            languages: [],
            tags: []
        };
    }
    render() {
        return (
            <div>
                <b>Add new component</b>
                <br/>

                ID <input type="text" name="id" placeholder="Component Id" onChange={e => this.setState({id: e.target.value})} /><br/>

                Label <input type="text" name="label" placeholder="Label" onChange={e => this.setState({label: e.target.value})} /><br/>
                Type <input type="text" name="type" placeholder="Type" onChange={e => this.setState({type: e.target.value})} /><br/>
                Systems <input type="text" name="systems" placeholder="Systems" onChange={e => this.setState({systems: e.target.value.split(' ')})} /><br/>
                Languages <input type="text" name="languages" placeholder="Languages" onChange={e => this.setState({languages: e.target.value.split(' ')})} /><br/>
                Tags <input type="text" name="tags" placeholder="Tags" onChange={e => this.setState({tags: e.target.value.split(' ')})} /><br/>


                SCM <input type="text" name="scmType" placeholder="Type" onChange={e => this.setState({scmType: e.target.value})} />
                <input type="text" name="scmUrl" placeholder="Repo URL" onChange={e => this.setState({scmUrl: e.target.value})} />
                <input type="text" name="scmPath" placeholder="Path" onChange={e => this.setState({scmPath: e.target.value})} /><br/>

                {this.props.error &&
                    <span style={{color: 'red'}}><i>Error: {this.props.error}</i></span>
                }
                {!this.props.processing
                    ? <input type="button" value="Add" onClick={e => this.props.onAdd(this.state)} />
                    : <span><i>Adding</i></span>
                }
                <input type="button" value="Cancel" onClick={e => this.props.onCancel()} />
            </div>
        )
    }
}


export { NewComponent }
export type { NewComponentType }
