import type {ComponentRefType} from "../component/ComponentType";
import type {InstanceRefType} from "../environment/InstanceType";

type VersionUsage = {
    version: string,
    dismissed: boolean,
    dependents: number
}

type LibraryType = {
    artifactId: string,
    versions: Array<VersionUsage>
}

type ComponentVersionType = {
    component: ComponentRefType,
    version: string,
    instances: Array<InstanceRefType>
}

type LibraryUsageType = {
    version: string,
    dismissed: boolean,
    components: Array<ComponentVersionType>
}

export type {LibraryType, ComponentVersionType, LibraryUsageType}