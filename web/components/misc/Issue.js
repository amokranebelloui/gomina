// @flow
import * as React from "react"
import type {IssueRefType} from "../work/WorkType";

type Props = {
    issue?: ?IssueRefType
}

class Issue extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const issue = this.props.issue;
        if (issue) {
            return (
                issue.issueUrl
                    ? (<a href={issue.issueUrl} target="_blank">{issue.issue}</a>)
                    : (<span>{issue.issue}</span>)
            )
        }
        return null
    }
}

export { Issue }