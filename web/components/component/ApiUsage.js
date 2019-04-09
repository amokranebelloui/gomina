// @flow
import * as React from "react"
import type {FunctionType} from "./ApiType";
import {ApiSource, containsManualSource, sortApiSource} from "./ApiSource";
import {Secure} from "../permission/Secure";

type Props = {
    usage: FunctionType,
    onManualUsageRemove?: () => void
}

class ApiUsage extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    removeManualSource() {
        this.props.onManualUsageRemove && this.props.onManualUsageRemove()
    }
    render() {
        const usage = this.props.usage;
        return (
            <div style={{marginTop: '4px', marginBottom: '4px'}}>
                {usage.name}
                [{usage.type}]
                <i style={{color: 'gray'}}>{usage.usage}</i>
                {sortApiSource(usage.sources).map(s =>
                    <ApiSource source={s} />
                )}
                <Secure permission="component.edit.usage">
                    {containsManualSource(usage.sources) &&
                        <button onClick={() => this.removeManualSource()}>-</button>
                    }
                </Secure>
            </div>
        );
    }
}

export { ApiUsage }