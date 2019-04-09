// @flow
import * as React from "react"

type Props = {
    source: string
}

class ApiSource extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    color(source: string) {
        return source === 'manual' ? 'gold' : 'lightgray'
    }
    render() {
        return (
            <span style={{
                backgroundColor: this.color(this.props.source), color: 'white', padding: '1px 3px',
                marginRight: '1px', marginLeft: '1px', borderRadius: 2
            }}>
                {this.props.source}
            </span>
        );
    }
}

function sortApiSource(sources: Array<string>): Array<string> {
    return sources && sources.sort((a, b) => a === 'manual' ? 1 : b === 'manual' ? -1 : a > b ? 1 : -1)
}

function containsManualSource(sources: Array<string>): boolean {
    return sources && sources.indexOf("manual") > -1
}

export { ApiSource, sortApiSource, containsManualSource }