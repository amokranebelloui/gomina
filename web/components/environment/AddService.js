// @flow
import * as React from "react"
import {InlineAdd} from "../common/InlineAdd";
import * as axios from "axios";
import type {EnvType} from "./Environment";
import {ServiceModeEditor} from "./ServiceModeEditor";

type Props = {
    env: string,
    onServiceAdded?: string => void,
}

type State = {
    svc?: ?string,
    type?: ?string,
    mode?: ?string,
    count?: ?string,
    componentId?: ?string
}

class AddService extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            svc: "",
            type: "",
            mode: "",
            count: "",
            componentId: ""
        };
    }
    clearState() {
        this.setState({
            svc: "",
            type: "",
            mode: "",
            count: "",
            componentId: ""
        })
    }
    addService() {
        return this.state.svc && this.state.mode
            ? axios.post('/data/instances/' + this.props.env + '/service/add?svcId=' + this.state.svc, this.state)
            : Promise.reject("Service Name/Mode are mandatory");
    };
    render() {
        return (
            <InlineAdd type="Service"
                       action={() => this.addService()}
                       onEnterAdd={() => this.clearState()}
                       onItemAdded={(data: any) => this.props.onServiceAdded && this.props.onServiceAdded(data.svc)}
                       editionForm={() =>
                           <div>
                               <span>New Service for <b>{this.props.env}</b></span>
                               <br/>
                               <input type="text" name="svc" placeholder="Service"
                                      value={this.state.svc}
                                      onChange={e => this.setState({svc: e.target.value})} />
                               <br/>
                               <input type="text" name="type" placeholder="Type"
                                      value={this.state.type}
                                      onChange={e => this.setState({type: e.target.value})} />
                               <br/>
                               <ServiceModeEditor mode={this.state.mode}
                                                  count={this.state.count}
                                                  onChanged={(mode, count) => this.setState({mode: mode, count: count})} />
                               <br/>
                               <input type="text" name="component" placeholder="Component"
                                      value={this.state.componentId}
                                      onChange={e => this.setState({componentId: e.target.value})} />
                           </div>
                       }
                       successDisplay={(data: any) =>
                           <div>
                               <b>{data ? (data.svc + " --> " + JSON.stringify(data)) : 'no data returned'}</b>
                           </div>
                       }
            />
        );
    }
}

export { AddService }
