// @flow
import * as React from "react"

type BranchDetailType = {
    name: string,
    origin?: ?string,
    originRevision?: ?string
}

type ComponentType = {
    id: string,
    artifactId?: ?string,
    label?: ?string,
    type?: ?string,
    owner?: ?string,
    critical?: ?number,
    systems?: ?Array<string>,
    languages?: ?Array<string>,
    tags?: ?Array<string>,
    scmType?: ?string,
    scmUrl?: ?string,
    scmPath?: ?string,
    scmLocation?: ?string,
    sonarServer?: ?string,
    sonarUrl?: ?string,
    jenkinsServer?: ?string,
    jenkinsJob?: ?string,
    jenkinsUrl?: ?string,
    buildNumber?: ?string,
    buildStatus?: ?string,
    buildTimestamp?: ?number,
    branches: Array<BranchDetailType>,
    docFiles?: ?Array<string>,
    changes?: ?number,
    latest?: ?string,
    released?: ?string,
    loc?: ?number,
    coverage?: ?number,
    lastCommit?: ?number, // date
    commitActivity?: ?number,
    commitToRelease?: ?number,
    disabled: boolean
}

type ComponentRefType = {
    id: string,
    label?: ?string,
    systems?: Array<string>,
}

export type {ComponentType, ComponentRefType, BranchDetailType}