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
    envName?: ?string
}

class AddEnvironment extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            envId: "",
            envName: "",
        };
    }
    clearState() {
        this.setState({
            envId: "",
            envName: "",
        })
    }
    addEnv() {
        return this.state.envId && this.state.envName
            ? axios.post('/data/envs/add?envId=' + this.state.envId, this.state.envName)
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
                               <span>Add Env</span>
                               <input type="text" name="id" placeholder="env Id"
                                      value={this.state.envId}
                                      onChange={e => this.setState({envId: e.target.value})} />
                               <input type="text" name="name" placeholder="env Name"
                                      value={this.state.envName}
                                      onChange={e => this.setState({envName: e.target.value})} />
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
