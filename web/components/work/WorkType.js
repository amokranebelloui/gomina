// @flow
import * as React from "react"
import type {ComponentRefType} from "../component/ComponentType";
import type {UserRefType} from "../misc/UserType";

type IssueRefType = {
    issue: string,
    issueUrl?: ?string
}

type WorkType = {
    id: string,
    label: string,
    type: ?string,
    issues: Array<IssueRefType>,
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
    type?: ?string,
    issues: Array<string>,
    people: Array<string>,
    components: Array<string>,
    dueDate?: ?string
}

export type { WorkType, WorkManifestType, ComponentWorkType, WorkDataType, IssueRefType }
