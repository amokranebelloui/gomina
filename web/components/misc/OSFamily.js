// @flow
import * as React from "react"

import linuxIcon from "./os-linux.png"
import macIcon from "./os-mac.png"
import windowsIcon from "./os-windows.png"
import dummyIcon from "./os-dummy.png"
import defaultIcon from "./os.png"

function iconForType(type: ?string) {
    switch (type) {
        case "linux" : return linuxIcon;
        case "macos" : return macIcon;
        case "windows" : return windowsIcon;
        case "dummy" : return dummyIcon;
        default : return defaultIcon;
    }
}


type Props = {
    osFamily?: ?string
}

class OSFamily extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        return (
            <img src={iconForType(this.props.osFamily)} width="24" height="24" style={{verticalAlign: 'middle'}} />
        );
    }
}

export { OSFamily }