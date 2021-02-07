// @flow
import React, {Fragment} from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import type {DiagramComponentType} from "../components/archidiagram/ArchiDiagram";
import {ArchiDiagram} from "../components/archidiagram/ArchiDiagram";
import {Container} from "../components/common/Container";
import type {ComponentRefType} from "../components/component/ComponentType";
import {Well} from "../components/common/Well";
import type {DiagramRefType} from "../components/archidiagram/DiagramEditor";
import {DiagramEditor} from "../components/archidiagram/DiagramEditor";
import {InlineAdd} from "../components/common/InlineAdd";
import Link from "react-router-dom/es/Link";
import type {DiagramType} from "../components/archidiagram/DiagramType";
import Route from "react-router-dom/es/Route";
import {EditableLabel} from "../components/common/EditableLabel";
import {TagCloud} from "../components/common/TagCloud";
import {joinTags, matchesList, splitTags} from "../components/common/utils";
import ls from "local-storage";
import {extendSystems} from "../components/system/system-utils";
import {ApplicationLayout} from "./common/ApplicationLayout";

type Props = {
    match: any
}

type State = {
    diagrams: Array<DiagramRefType>,
    systemRefs: Array<string>,
    selectedSystems: Array<string>,
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
            systemRefs: [],
            selectedSystems: splitTags(ls.get('components.systems')) || [],
            componentRefs: [],
            selectedDiagram: null,
            selectedComponent: null,
            newDiagram: null
        };
    }

    componentDidMount() {
        document.title = 'Architecture Diagrams';
        this.retrieveComponentRefs();
        this.retrieveSystems();
        this.retrieveDiagrams();
        this.retrieveDiagram(this.props.match.params.id);
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
    retrieveComponentRefs() {
        const thisComponent = this;
        axios.get('/data/components/refs')
            .then(response => thisComponent.setState({componentRefs: response.data}))
            .catch(error => thisComponent.setState({componentRefs: []}));
    }
    retrieveSystems() {
        const thisComponent = this;
        axios.get('/data/systems/refs')
            .then(response => thisComponent.setState({systemRefs: response.data}))
            .catch(error => thisComponent.setState({systemRefs: []}));
    }
    selectedSystemsChanged(systems: Array<string>) {
        console.info("selected systems", systems);
        this.setState({selectedSystems: systems});
        ls.set('components.systems', joinTags(systems));
    }

    retrieveDiagrams() {
        const thisComponent = this;
        axios.get('/data/diagram/all')
            .then(response => thisComponent.setState({diagrams: response.data}))
            .catch(error => thisComponent.setState({diagrams: []}));
    }
    retrieveDiagram(diagramId: string) {
        diagramId && axios.get('/data/diagram/data?diagramId=' + diagramId)
            .then(response => {
                console.log(":diagram data", response.data);
                document.title = response.data.name + ' - Diagram';
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
    changeName(diagramId: string, name: string) {
        console.info(":change name");
        axios.put('/data/diagram/name?diagramId=' + diagramId, name)
            .then(() => this.retrieveDiagram(diagramId))
            .catch(error => console.log(":change name error", error.response));
    }
    changeDescription(diagramId: string, desc: string) {
        console.info(":change name");
        axios.put('/data/diagram/description?diagramId=' + diagramId, desc)
            .then(() => this.retrieveDiagram(diagramId))
            .catch(error => console.log(":change desc error", error.response));
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
                const this_ = this;
                const x = Math.floor((Math.random() * 400) + 30);
                const y = Math.floor((Math.random() * 200) + 30);
                const dComp = {name: component.id, x: x, y: y};
                const newComponents = [].concat(diag.components, [dComp]);
                let newDiag = Object.assign({}, diag, {components: newComponents});
                this.setState({selectedDiagram: newDiag});
                console.info(":add component");
                diag &&
                axios.post('/data/diagram/node/update?diagramId=' + diag.diagramId, dComp)
                    .then(response => {
                        console.log(":added", response.data);
                        this_.retrieveDiagram(diag.diagramId)
                    })
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
        const componentRefs = filterComponents(this.state.componentRefs, this.state.selectedSystems)
            .sort((a,b) => (a.label||'') > (b.label||'') ? 1 : -1);
        return (
            <ApplicationLayout title="Architecture Diagram"
               header={() =>
                   <Fragment>
                       {!diagram &&
                           <p>Set of blueprints describing system architecture</p>
                       }
                       {diagram &&
                           <div style={{width: '100%', verticalAlign: 'top'}}>
                               <div style={{display: 'block'}}>
                                   <div style={{float: 'right'}}>
                                       <Route render={({history}) => (
                                           <input type="button" value="Delete" onClick={e => this.deleteDiagram(diagram.diagramId, history)} />
                                       )}/>
                                   </div>
                                   <EditableLabel label={diagram.name} style={{fontSize: 16, fontWeight: 'bold'}}  altText={'<>'}
                                                  onLabelEdited={name => this.changeName(diagram.diagramId, name)}/>
                                   <br/>
                                   <EditableLabel label={diagram.description} style={{width: '99%'}}  altText={'<>'}
                                                  onLabelEdited={desc => this.changeDescription(diagram.diagramId, desc)}/>
                                   <div style={{clear: 'both'}} />
                               </div>
                           </div>
                       }
                   </Fragment>
               }
               main={() =>
                   <Fragment>
                       {!diagram &&
                       <Fragment>
                           {this.state.diagrams.map(d =>
                               <div key={d.diagramId}>
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
                   </Fragment>
               }
               sidePrimary={() =>
                   <Fragment>
                       <h3>Control Panel</h3>
                       <b>Systems:</b>
                       <TagCloud tags={this.state.systemRefs} selected={this.state.selectedSystems}
                                 selectionChanged={s => this.selectedSystemsChanged(s)} />
                       <br/>
                   </Fragment>
               }
               sideSecondary={() =>
                   <Fragment>
                       {componentRefs.map(c =>
                           <div key={c.id}>
                               {diagram && diagram.components.filter(cc => cc.name === c.id).length === 0 ?
                                   <Fragment>
                                       {c.label} - {c.id} &nbsp;
                                       <button onClick={e => this.addData(c)}>Add</button>
                                   </Fragment>
                                   :
                                   <Fragment>
                                       <span style={{color: 'lightgray'}}>{c.label} - {c.id}</span>
                                   </Fragment>
                               }
                           </div>
                       )}
                   </Fragment>
               }
            />
        );
    }
}

function filterComponents(components: Array<ComponentRefType>, systems: Array<string>) {
    return components.filter(c =>
        //matchesSearch(c, search) &&
        matchesList(extendSystems(c.systems), systems)
    );
}

export { ArchitectureApp };