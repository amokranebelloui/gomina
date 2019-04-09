// @flow
import * as React from "react"
import type {FunctionType} from "./ApiType";
import {ApiSource, containsManualSource, sortApiSource} from "./ApiSource";
import {Secure} from "../permission/Secure";

type Props = {
    api: FunctionType,
    onManualApiRemove?: () => void
}

class ApiDefinition extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    removeManualSource() {
        this.props.onManualApiRemove && this.props.onManualApiRemove()
    }
    render() {
        const api = this.props.api;
        return (
            <div style={{marginTop: '4px', marginBottom: '4px'}}>
                {api.name}
                [{api.type}]
                {sortApiSource(api.sources).map(s =>
                    <ApiSource source={s} />
                )}
                <Secure permission="component.edit.api">
                    {containsManualSource(api.sources) &&
                        <button onClick={() => this.removeManualSource()}>-</button>
                    }
                </Secure>
            </div>
        );
    }
}

export { ApiDefinition }