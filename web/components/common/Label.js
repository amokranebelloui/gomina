// @flow

import * as React from "react"

type Props = {
    label: ?string,
    altText?: ?string,
    style?: ?any,
}


class Label extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    componentWillReceiveProps(nextProps: Props) {
        console.info("EditableLabel will receive props", this.props, nextProps);
    }
    render() {
        return (
            this.props.label
                ? <span>{this.props.label}</span>
                : <span style={{opacity: .5, textDecoration: 'italic'}}>{this.props.altText || '?'}</span>
        )
    }
}

export { Label }