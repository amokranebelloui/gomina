// @flow
import * as React from "react"
import {InlineAdd} from "../common/InlineAdd";
import * as axios from "axios";
import {ServiceModeEditor} from "./ServiceModeEditor";
import {Autocomplete} from "../common/AutoComplete";
import type {ComponentRefType} from "../component/ComponentType";

type Props = {
    env: string,
    components: Array<ComponentRefType>,
    onServiceAdded?: string => void,
}

type State = {
    svc?: ?string,
    type?: ?string,
    mode?: ?string,
    activeCount?: ?string,
    component?: ?ComponentRefType
}

class AddService extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            svc: "",
            type: "",
            mode: "",
            activeCount: "",
            component: null
        };
    }
    clearState() {
        this.setState({
            svc: "",
            type: "",
            mode: "",
            activeCount: "",
            component: null
        })
    }
    addService() {
        const data = Object.assign({}, this.state, {componentId: this.state.component && this.state.component.id});
        return this.state.svc && this.state.mode
            ? axios.post('/data/instances/' + this.props.env + '/service/add?svcId=' + this.state.svc, data)
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
                                                  count={this.state.activeCount}
                                                  onChanged={(mode, count) => this.setState({mode: mode, activeCount: count})} />
                               <br/>
                               <Autocomplete value={this.state.component}
                                             suggestions={this.props.components}
                                             idGetter={s => s.id} labelGetter={s => s.label}
                                             onChange={c => this.setState({component: c})} />
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
