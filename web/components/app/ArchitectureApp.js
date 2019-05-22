// @flow
import React from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import type {DiagramComponentType, DiagramDependencyType} from "../archidiagram/ArchiDiagram";
import {ArchiDiagram} from "../archidiagram/ArchiDiagram";
import {Container} from "../common/Container";
import type {ComponentRefType} from "../component/ComponentType";
import {Well} from "../common/Well";

type Props = {

}

type State = {
    componentRefs: Array<ComponentRefType>,
    components: Array<DiagramComponentType>,
    dependencies: Array<DiagramDependencyType>,
    selectedComponent: ?string
}

class ArchitectureApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            componentRefs: [],
            components: [],
            dependencies: [],
            selectedComponent: null
        };
    }

    componentDidMount() {
        this.retrieveDiagram();
        this.retrieveComponentRefs();
    }
    retrieveComponentRefs() {
        const thisComponent = this;
        axios.get('/data/components/refs')
            .then(response => thisComponent.setState({componentRefs: response.data}))
            .catch(error => thisComponent.setState({componentRefs: []}));
    }

    retrieveDiagram() {
        axios.get('/data/diagram/data')
            .then(response => {
                console.log(":diagram data", response.data);
                this.setState({components: response.data.components, dependencies: response.data.dependencies});
            })
            .catch(function (error) {
                console.log(":diagram data error", error.response);
            });
    }
    addData(component: ComponentRefType) {
        if (!this.state.components.find(c => c.name === component.id)) {
            const newComponents = [].concat(this.state.components, [{name: component.id, x: 50, y: 50}]);
            this.setState({components: newComponents});
            console.info(":add component");
        }
    }

    removeData(c: string) {
        //this.state.dependencies.pop();
        //const newDeps = this.state.dependencies;
        //this.setState({dependencies: dependencies2});
        this.setState({
            components: this.state.components.filter(i => i.name !== c),
            dependencies: this.state.dependencies.filter(d => d.from !== c && d.to !== c)
        });
        console.info(":remove component");
    }

    componentMoved(d: DiagramComponentType) {
        console.info(":component moved", d);

        axios.post('/data/diagram/update', d)
            .then(response => {
                console.log(":updated", response.data);
            })
            .catch(function (error) {
                console.log(":update error", error.response);
            });
    }

    render() {
        const selectedComponent = this.state.selectedComponent;
        return (
            <AppLayout title="Architecture Diagram">
                <PrimarySecondaryLayout>
                    <Container>
                        <div style={{width: '100%', verticalAlign: 'top'}}>
                            <div style={{width: '50%', boxSizing: 'border-box', display: "inline-block", verticalAlign: 'top'}}>
                                <ArchiDiagram components={this.state.components}
                                              dependencies={this.state.dependencies}
                                              onComponentSelected={c => this.setState({selectedComponent: c.name})}
                                              onLinkSelected={d => console.info(":selected", d)}
                                              onComponentMoved={c => this.componentMoved(c)}/>
                            </div>

                        </div>
                    </Container>
                    <div>
                        <h3>Control Panel</h3>
                        <Well block>
                            {selectedComponent &&
                                <div>
                                    {selectedComponent}
                                    <button onClick={e => this.removeData(selectedComponent)}>Remove</button>
                                </div>
                            }
                        </Well>
                        {this.state.componentRefs.map(c =>
                            <div key={c.id}>
                                {c.label} - {c.id}
                                <button onClick={e => this.addData(c)}>Add</button>
                            </div>
                        )}
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}

export { ArchitectureApp };