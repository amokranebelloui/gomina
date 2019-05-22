// @flow
import React from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import type {DiagramComponentType, DiagramDependencyType} from "../archidiagram/ArchiDiagram";
import {ArchiDiagram} from "../archidiagram/ArchiDiagram";
import {Container} from "../common/Container";

type Props = {

}

type State = {
    components: Array<DiagramComponentType>,
    dependencies: Array<DiagramDependencyType>
}

class ArchitectureApp extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            components: [],
            dependencies: []
        };
        //$FlowFixMe
        this.addData = this.addData.bind(this);
        //$FlowFixMe
        this.removeData = this.removeData.bind(this);
        //$FlowFixMe
        this.componentMoved = this.componentMoved.bind(this);
    }

    componentDidMount() {
        this.retrieveDiagram();
    }
    retrieveDiagram() {
        axios.get('/data/diagram/data')
            .then(response => {
                console.log("diagram data", response.data);
                this.setState({components: response.data.components, dependencies: response.data.dependencies});
            })
            .catch(function (error) {
                console.log("diagram data error", error.response);
            });
    }
    addData() {
        //const a = Math.floor((Math.random() * 10));
        //const b = Math.floor((Math.random() * 10));
        //const newDeps = [].concat(this.state.dependencies, [{from: a, to: b}]);
        //this.setState({dependencies: dependencies});
        console.info("Reload: add");
    }

    removeData() {
        //this.state.dependencies.pop();
        //const newDeps = this.state.dependencies;
        //this.setState({dependencies: dependencies2});
        console.info("Reload: rem");
    }

    componentMoved(d: DiagramComponentType) {
        console.info("moved 2", d);

        axios.post('/data/diagram/update', d)
            .then(response => {
                console.log("reloaded", response.data);
            })
            .catch(function (error) {
                console.log("reload error", error.response);
            });
    }

    render() {
        return (
            <AppLayout title="Architecture Diagram">
                <PrimarySecondaryLayout>
                    <Container>
                        <div style={{width: '100%', verticalAlign: 'top'}}>
                            <div style={{width: '50%', boxSizing: 'border-box', display: "inline-block", verticalAlign: 'top'}}>
                                <ArchiDiagram components={this.state.components}
                                              dependencies={this.state.dependencies}
                                              onLinkSelected={d => console.info("selected", d)}
                                              onComponentMoved={this.componentMoved}/>
                            </div>

                        </div>
                    </Container>
                    <div>
                        <h3>Control Panel</h3>
                        <button onClick={this.addData}>Add</button>
                        <button onClick={this.removeData}>Remove</button>
                    </div>
                </PrimarySecondaryLayout>
            </AppLayout>
        );
    }
}

export { ArchitectureApp };