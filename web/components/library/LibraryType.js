import type {ComponentRefType} from "../component/ComponentType";
import type {InstanceRefType} from "../environment/InstanceType";

type VersionUsage = {
    version: string,
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
    components: Array<ComponentVersionType>
}

export type {LibraryType, ComponentVersionType, LibraryUsageType}