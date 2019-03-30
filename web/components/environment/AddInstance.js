// @flow
import * as React from "react"
import {InlineAdd} from "../common/InlineAdd";
import * as axios from "axios";

type Props = {
    env: string,
    svc: string,
    onInstanceAdded?: string => void,
}

type State = {
    id?: ?string,
    host?: ?string,
    folder?: ?string
}

class AddInstance extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            id: "",
            host: "",
            folder: ""
        };
    }
    clearState() {
        this.setState({
            id: "",
            host: "",
            folder: ""
        })
    }
    addInstance() {
        return this.state.id
            ? axios.post('/data/instances/add?instanceId=' + this.state.id + '&svc=' + this.props.svc + '&env=' + this.props.env, this.state)
            : Promise.reject("Instance Id is mandatory");
    };
    render() {
        return (
            <InlineAdd type="Instance"
                       action={() => this.addInstance()}
                       onEnterAdd={() => this.clearState()}
                       onItemAdded={(data: any) => this.props.onInstanceAdded && this.props.onInstanceAdded(data.id)}
                       editionForm={() =>
                           <div>
                               <span>Add Instance for <b>{this.props.env}</b>/<b>{this.props.svc}</b></span>
                               <br/>
                               <input type="text" name="id" placeholder="Instance Id"
                                      value={this.state.id}
                                      onChange={e => this.setState({id: e.target.value})} />
                               <br/>
                               <input type="text" name="host" placeholder="Host"
                                      value={this.state.host}
                                      onChange={e => this.setState({host: e.target.value})} />
                               <br/>
                               <input type="text" name="folder" placeholder="Folder"
                                      value={this.state.folder}
                                      onChange={e => this.setState({folder: e.target.value})} />
                           </div>
                       }
                       successDisplay={(data: any) =>
                           <div>
                               <b>{data ? (data.id + " --> " + JSON.stringify(data)) : 'no data returned'}</b>
                           </div>
                       }
            />
        );
    }
}

export { AddInstance }
