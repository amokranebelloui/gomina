// @flow
import * as React from "react"

type WorkType = {
    id: string,
    label: string,
    type: ?string,
    jira: ?string,
    jiraUrl: ?string,
    status: string,
    people: Array<string>,
    components: Array<string>,
    archived: boolean
}

type WorkManifestType = {
    work: ?WorkType,
    details: Array<ComponentWorkType>
}

type ComponentWorkType = {
    componentId: string, 
    commits: Array<any> // FIXME Replace by CommitDetail
}

type WorkDataType = {
    label: ?string,
    type: ?string,
    jira: ?string,
    people: Array<string>,
    components: Array<string>
}

export type { WorkType, WorkManifestType, ComponentWorkType, WorkDataType }
