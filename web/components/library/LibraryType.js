import type {ComponentRefType} from "../component/ComponentType";
import type {InstanceRefType} from "../environment/InstanceType";

type LibraryType = {
    artifactId: string,
    versions: Array<string>
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