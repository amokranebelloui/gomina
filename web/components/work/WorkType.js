// @flow
import * as React from "react"
import type {ComponentRefType} from "../component/ComponentType";
import type {UserRefType} from "../misc/UserType";

type WorkType = {
    id: string,
    label: string,
    type: ?string,
    jira: ?string,
    jiraUrl: ?string,
    status: string,
    people: Array<UserRefType>,
    components: Array<ComponentRefType>,
    creationDate: ?Date,
    dueDate: ?string,
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
    components: Array<string>,
    dueDate: ?string
}

export type { WorkType, WorkManifestType, ComponentWorkType, WorkDataType }
