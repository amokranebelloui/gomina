// @flow
import * as React from "react"

type FunctionType = {
    name: string,
    type: string,
    usage: ?string,
    sources: Array<string>
}

type ApiDefinitionType = "definition" | "usage"

export type {FunctionType, ApiDefinitionType}
