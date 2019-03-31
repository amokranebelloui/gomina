// @flow
import * as React from "react"
import type {EnvType} from "./Environment";

type Props = {
    env: EnvType,
}

class EnvDetail extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const selectedEnv = this.props.env;
        return (
            <div>
                {selectedEnv.env}<br/>
                {selectedEnv.type}<br/>
                {selectedEnv.description}<br/>
                {selectedEnv.monitoringUrl}<br/>
                <b>{selectedEnv.active ? 'Enabled' : 'Disabled'}</b><br/>
            </div>
        );
    }
}

export { EnvDetail }