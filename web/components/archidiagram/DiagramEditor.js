// @flow
import * as React from "react"
import type {DiagramRefType} from "./DiagramType";

type Props = {
    diagram?: DiagramRefType,
    onChange?: (string, DiagramRefType) => void
}

type State = {
    diagramId: string,
    name: string,
    description: ?string,
}


class DiagramEditor extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            diagramId: this.props.diagram && this.props.diagram.diagramId || "",
            name: this.props.diagram && this.props.diagram.name || "",
            description: this.props.diagram && this.props.diagram.description,
        }
    }
    changeDiagramId(value: string) {
        this.setState({diagramId: value});
        this.notifyChange({diagramId: value});
    }
    changeName(value: string) {
        this.setState({name: value});
        this.notifyChange({name: value});
    }
    changeDescription(val: ?string) {
        this.setState({description: val});
        this.notifyChange({description: val});
    }
    notifyChange(delta: any) {
        const fromState: DiagramRefType = {
            diagramId: this.state.diagramId,
            name: this.state.name,
            description: this.state.description
        };
        const data = Object.assign({}, fromState, delta);
        this.props.onChange && this.props.onChange(this.state.diagramId, data)
    }
    render() {
        return (
            <div>
                <b>Id </b>
                <input type="text" name="diagramId" placeholder="Id"
                       value={this.state.diagramId}
                       onChange={e => this.changeDiagramId(e.target.value)} />
                <br/>
                <b>Name </b>
                <input type="text" name="name" placeholder="Name"
                       value={this.state.name}
                       onChange={e => this.changeName(e.target.value)} />
                <br/>
                <b>Description </b>
                <input type="text" name="description" placeholder="Description"
                       value={this.state.description}
                       onChange={e => this.changeDescription(e.target.value)} />
                <br/>
            </div>
        );
    }
}

export { DiagramEditor }
export type { DiagramRefType }