// @flow
import * as React from "react"
import type {WorkStatusType} from "./WorkType";
import "./WorkStatus.css"

type Props = {
    status: WorkStatusType,
    onStatusChange?: (status: WorkStatusType) => void
}

const statuses = ['OFF', 'SPEC', 'DEV', 'TESTING', 'DONE', 'DEPLOYMENT', 'COMPLETED'];

class WorkStatus extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        return (
            <span className="work-status">
                {statuses.map(status =>
                    <span style={{
                        backgroundColor: this.props.status === status ? 'gray' : null,
                        color: this.props.status === status ? 'white' : null
                    }}
                    onClick={e => this.props.onStatusChange && this.props.onStatusChange(status)}>
                        {status}
                    </span>
                )}
            </span>
        );
    }
}

export { WorkStatus }