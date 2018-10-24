// @flow
import * as React from "react"

type BranchDetailType = {
    name: string,
    origin?: ?string,
    originRevision?: ?string
}

type ComponentType = {
    id: string,
    label?: ?string,
    type?: ?string,
    owner?: ?string,
    critical?: ?number,
    systems?: ?Array<string>,
    languages?: ?Array<string>,
    tags?: ?Array<string>,
    scmType?: ?string,
    scmLocation?: ?string,
    mvn?: ?string,
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
    commitActivity?: ?number
}

type ComponentRefType = {
    id: string,
    label?: ?string,
}

export type {ComponentType, ComponentRefType, BranchDetailType}