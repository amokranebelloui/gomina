import React from "react";
import {Badge} from "../common/Badge";

class UnreleasedChangeCount extends React.Component {
    render() {
        const changesCount = this.props.changes;
        const hasChanges = changesCount > 0;
        //const backgroundColor = hasChanges ? '#EAA910' : '#F2E18F';
        return (
            hasChanges && <Badge backgroundColor='#EAA910' color='white'>{changesCount}</Badge>
        )
    }
}

export {UnreleasedChangeCount}
