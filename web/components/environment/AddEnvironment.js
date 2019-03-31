// @flow
import * as React from "react"
import {InlineAdd} from "../common/InlineAdd";
import * as axios from "axios";
import type {EnvType} from "./Environment";

type Props = {
    onEnvironmentAdded?: string => void,
}

type State = {
    envId?: ?string,
    type: string,
    description: string,
    monitoringUrl: string,
}

class AddEnvironment extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            envId: "",
            type: "",
            description: "",
            monitoringUrl: ""
        };
    }
    clearState() {
        this.setState({
            envId: "",
            type: "",
            description: "",
            monitoringUrl: ""
        })
    }
    addEnv() {
        return this.state.envId
            ? axios.post('/data/envs/add?envId=' + this.state.envId, this.state)
            : Promise.reject("Env Id/Name are mandatory");
    };
    render() {
        return (
            <InlineAdd type="Environment"
                       action={() => this.addEnv()}
                       onEnterAdd={() => this.clearState()}
                       onItemAdded={(data: EnvType) => this.props.onEnvironmentAdded && this.props.onEnvironmentAdded(data.env)}
                       editionForm={() =>
                           <div>
                               <span>New Env</span>
                               <br/>
                               <input type="text" name="id" placeholder="env Id"
                                      value={this.state.envId}
                                      onChange={e => this.setState({envId: e.target.value})} />
                               <br/>
                               <input type="text" name="type" placeholder="Type"
                                      value={this.state.type}
                                      onChange={e => this.setState({type: e.target.value})} />
                               <br/>
                               <input type="text" name="description" placeholder="Description"
                                      value={this.state.description}
                                      onChange={e => this.setState({description: e.target.value})} />
                               <br/>
                               <input type="text" name="monitoringUrl" placeholder="Monitoring URL"
                                      value={this.state.monitoringUrl}
                                      style={{width: '200px'}}
                                      onChange={e => this.setState({monitoringUrl: e.target.value})} />
                           </div>
                       }
                       successDisplay={(data: EnvType) =>
                           <div>
                               <b>{data ? (data.env + " --> " + JSON.stringify(data)) : 'no data returned'}</b>
                           </div>
                       }
            />
        );
    }
}

export { AddEnvironment }
