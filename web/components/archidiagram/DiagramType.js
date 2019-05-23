import type {DiagramComponentType, DiagramDependencyType} from "./ArchiDiagram";

type DiagramRefType = {
    diagramId: string,
    name: string,
    description: ?string,
}

type DiagramType = {
    diagramId: string,
    name: string,
    description: ?string,
    components: Array<DiagramComponentType>,
    dependencies: Array<DiagramDependencyType>
}

export type {DiagramType, DiagramRefType}