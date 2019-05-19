import type {VersionType} from "../common/version-utils";

type InstanceRefType = {
    id: string,
    env: string,
    name: string,
    running: VersionType,
    deployed: VersionType
}

export type {InstanceRefType}