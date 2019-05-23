// @flow
import React, {Fragment} from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import type {DiagramComponentType, DiagramDependencyType} from "../archidiagram/ArchiDiagram";
import {ArchiDiagram} from "../archidiagram/ArchiDiagram";
import {Container} from "../common/Container";
import type {ComponentRefType} from "../component/ComponentType";
import {Well} from "../common/Well";
import {DiagramEditor} from "../archidiagram/DiagramEditor";
import type {DiagramRefType} from "../archidiagram/DiagramEditor";
import {InlineAdd} from "../common/InlineAdd";
import Link from "react-router-dom/es/Link";
import type {DiagramType} from "../archidiagram/DiagramType";
import Route from "react-router-dom/es/Route";

type Props = {
    match: any
}

type State = {
    diagrams: Array<DiagramRefType>,
    componentRefs: Array<ComponentRefType>,
    selectedDiagram: ?DiagramType,
    selectedComponent: ?string,

    newDiagram: ?DiagramRefType
}

class ArchitectureApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            diagrams: [],
            componentRefs: [],
            selectedDiagram: null,
            selectedComponent: null,
            newDiagram: null
        };
    }

    componentDidMount() {
        this.retrieveDiagrams();
        this.retrieveDiagram(this.props.match.params.id);
        this.retrieveComponentRefs();
    }
    componentWillReceiveProps(nextProps: Props) {
        const newDiagramId = nextProps.match.params.id;
        //this.setState({diagramId: newDiagramId});
        if (this.props.match.params.id !== newDiagramId) {
            newDiagramId
                ? this.retrieveDiagram(newDiagramId)
                : this.setState({selectedDiagram: null});
        }
    }
    retrieveDiagrams() {
        const thisComponent = this;
        axios.get('/data/diagram/all')
            .then(response => thisComponent.setState({diagrams: response.data}))
            .catch(error => thisComponent.setState({diagrams: []}));
    }

    retrieveComponentRefs() {
        const thisComponent = this;
        axios.get('/data/components/refs')
            .then(response => thisComponent.setState({componentRefs: response.data}))
            .catch(error => thisComponent.setState({componentRefs: []}));
    }

    retrieveDiagram(diagramId: string) {
        diagramId && axios.get('/data/diagram/data?diagramId=' + diagramId)
            .then(response => {
                console.log(":diagram data", response.data);
                this.setState({selectedDiagram: response.data});
            })
            .catch(function (error) {
                console.log(":diagram data error", error.response);
            });
    }

    addDiagram() {
        console.info(":add diagram");
        const newDiagram = this.state.newDiagram;
        return newDiagram && axios.post('/data/diagram/add?diagramId=' + newDiagram.diagramId, newDiagram);
    }
    deleteDiagram(diagramId: string, history: any) {
        console.info(":del diagram");
        axios.delete('/data/diagram/delete?diagramId=' + diagramId)
            .then(response => {
                console.log(":delete", response.data);
                history.push('/architecture');
                this.retrieveDiagrams();
            })
            .catch(error => console.log(":delete error", error.response));
    }

    addData(component: ComponentRefType) {
        const diag = this.state.selectedDiagram;
        if (diag) {
            if (!diag.components.find(c => c.name === component.id)) {
                const dComp = {name: component.id, x: 50, y: 50};
                const newComponents = [].concat(diag.components, [dComp]);
                let newDiag = Object.assign({}, diag, {components: newComponents});
                this.setState({selectedDiagram: newDiag});
                console.info(":add component");
                diag &&
                axios.post('/data/diagram/node/update?diagramId=' + diag.diagramId, dComp)
                    .then(response => console.log(":added", response.data))
                    .catch(error => console.log(":add error", error.response));
            }
        }
    }
    removeData(c: string) {
        //this.state.dependencies.pop();
        //const newDeps = this.state.dependencies;
        //this.setState({dependencies: dependencies2});
        const diag = this.state.selectedDiagram;
        if (diag) {
            let newDiag = Object.assign({}, diag, {
                components: diag.components.filter(i => i.name !== c),
                dependencies: diag.dependencies.filter(d => d.from !== c && d.to !== c)
            });
            this.setState({selectedDiagram: newDiag});
            this.state.selectedDiagram &&
            axios.delete('/data/diagram/node/remove?diagramId=' + this.state.selectedDiagram.diagramId, {data: {name: c}})
                .then(response => console.log(":deleted", response.data))
                .catch(error => console.log(":delete error", error.response));
            console.info(":remove component");
        }
    }
    updateData(d: DiagramComponentType) {
        console.info(":component moved", d);

        const diag = this.state.selectedDiagram;
        if (diag) {
            axios.post('/data/diagram/node/update?diagramId=' + diag.diagramId, d)
                .then(response => console.log(":updated", response.data))
                .catch(error => console.log(":update error", error.response));
        }
    }

    render() {
        const selectedComponent = this.state.selectedComponent;
        const diagram = this.state.selectedDiagram;
        const componentRefs = this.state.componentRefs.sort((a,b) => (a.label||'') > (b.label||'') ? 1 : -1);
        return (
            <AppLayout title="Architecture Diagram">
                <PrimarySecondaryLayout>
                    <Container>
                        {!diagram &&
                            <Fragment>
                                <p>Set of blueprints describing system architecture</p>
                                <br/>
                                {this.state.diagrams.map(d =>
                                <div>
                                    <Link to={'/architecture/' + d.diagramId} >
                                    {d.name} - {d.description}
                                    </Link>
                                </div>
                                )}
                                <br/>
                                <InlineAdd type="Diagram"
                                           action={() => this.addDiagram()}
                                           onEnterAdd={() => this.setState({newDiagram: null})}
                                           onItemAdded={() => this.retrieveDiagrams()}
                                           editionForm={() =>
                                               <DiagramEditor onChange={(id, d) => this.setState({newDiagram: d})} />
                                           }
                                           successDisplay={(data: any) =>
                                               <div>
                                                   <b>{data ? (data + " --> " + JSON.stringify(data)) : 'no data returned'}</b>
                                               </div>
                                           }
                                />
                            </Fragment>
                        }
                        {diagram &&
                            <div style={{width: '100%', verticalAlign: 'top'}}>
                                <div style={{display: 'block'}}>
                                    <div style={{float: 'right'}}>
                                        <Route render={({history}) => (
                                            <input type="button" value="Delete" onClick={e => this.deleteDiagram(diagram.diagramId, history)} />
                                        )}/>
                                    </div>
                                    <b>{diagram.name}</b>
                                    <p>{diagram.description}</p>
                                    <div style={{clear: 'both'}}></div>
                                </div>
                                <ArchiDiagram components={diagram.components}
                                              dependencies={diagram.dependencies}
                                              onComponentSelected={c => this.setState({selectedComponent: c.name})}
                                              onLinkSelected={d => console.info(":selected", d)}
                                              onComponentMoved={c => this.updateData(c)} />
                                {selectedComponent &&
                                <div>
                                    {selectedComponent}
                                    <button onClick={e => this.removeData(selectedComponent)}>Remove</button>
                                </div>
                                }
                            </div>
                        }
                    </Container>
                    <div>
                        <h3>Control Panel</h3>
                        <Well block>

                        </Well>
                        <Well block>
                            {componentRefs.map(c =>
                                <div key={c.id}>
                                    {c.label} - {c.id}
                                    <button onClick={e => this.addData(c)}>Add</button>
                                </div>
                            )}
                        </Well>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}

export { ArchitectureApp };