// @flow
import * as React from "react"

type HostType = {
    host: string,
    managed: boolean,
    dataCenter?: ?string,
    group?: ?string,
    type?: ?string,
    osFamily?: ?string,
    os?: ?string,
    tags?: Array<string>,

    username?: ?string,
    passwordAlias?: ?string,
    proxyUser?: ?string,
    proxyHost?: ?string,
    sudo?: ?string,

    unexpected: Array<string>
}

type HostDataType = {
    dataCenter?: ?string,
    group?: ?String,
    type?: ?string,
    osFamily?: ?string,
    os?: ?string,
    tags?: Array<string>
}

type HostConnectivityDataType = {
    username?: ?string,
    passwordAlias?: ?string,
    proxyUser?: ?string,
    proxyHost?: ?string,
    sudo?: ?string
}

export type { HostType, HostDataType, HostConnectivityDataType }
