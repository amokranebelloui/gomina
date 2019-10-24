// @flow
import * as React from "react"
import type {VersionType} from "../common/version-utils";

type BranchDetailType = {
    name: string,
    origin?: ?string,
    originRevision?: ?string,
    buildServer: string,
    buildJob?: ?string,
    buildUrl?: ?string,
    buildNumber?: ?string,
    buildStatus?: ?string,
    buildTimestamp?: ?number,
    dismissed?: boolean
}

type ComponentType = {
    id: string,
    artifactId?: ?string,
    label?: ?string,
    type?: ?string,
    inceptionDate?: ?string,
    owner?: ?string,
    criticity?: ?string,
    systems?: ?Array<string>,
    languages?: ?Array<string>,
    tags?: ?Array<string>,
    scmType?: ?string,
    scmUrl?: ?string,
    scmPath?: ?string,
    scmLocation?: ?string,
    scmUsername?: ?string,
    scmPasswordAlias?: ?string,
    hasMetadata?: ?boolean,
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
    latest?: ?VersionType,
    released?: ?VersionType,
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