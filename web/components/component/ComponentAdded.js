// @flow

import * as React from "react"
import type {ComponentType} from "./ComponentType";

type Props = {
    component: ComponentType
}

class ComponentAdded extends React.Component<Props> {
    render() {
        const {component} = this.props;
        return (
            <div>
                Component {component.id} Added!<br/>
                {component.label}
            </div>
        )
    }
}

export { ComponentAdded }
