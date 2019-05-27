// @flow
import * as React from "react"
import {EditableLabel} from "../common/EditableLabel";
import {Label} from "../common/Label";
import {Secure} from "../permission/Secure";
import {StarRating} from "../common/StarRating";
import type {ComponentType} from "./ComponentType";

type Props = {
    component: ComponentType,
    knowledge: ?number,
    onLabelEdited: (componentId: string, label: string) => void,
    onArtifactIdEdited: (componentId: string, artifactId: string) => void,
    onKnowledgeChange: (componentId: string, knowledge: number) => void,
    onReload: (componentId: string) => void,
    onEnable: (componentId: string) => void,
    onDisable: (componentId: string) => void,
    onDelete: (componentId: string) => void
}

class ComponentBanner extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const component = this.props.component;
        return (
            <div>
                <span title={component.id}>

                    <Secure permission="component.edit" fallback={
                       <span style={{fontSize: 14, fontWeight: 'bold'}}>{component.label}</span>
                    }>
                        <EditableLabel label={component.label} style={{fontSize: 14, fontWeight: 'bold'}}
                                       altText={'no label'}
                                       onLabelEdited={l => this.props.onLabelEdited(component.id, l)}/>
                    </Secure>
                    &nbsp;
                    <StarRating value={this.props.knowledge} editable={true}
                                onRatingChange={k => this.props.onKnowledgeChange && this.props.onKnowledgeChange(component.id, k)}/>
                    &nbsp;
                    <button onClick={e => this.props.onReload(component.id)}>RELOAD</button>
                    {component.disabled &&
                        <span>
                            &nbsp;
                            <s>DISABLED</s>
                            <Secure permission="component.delete">
                                <button onClick={e => this.props.onDelete(component.id)}>Delete</button>
                            </Secure>
                        </span>
                    }
                </span>
                <div style={{float: 'right'}}>
                    <Secure permission="component.disable">
                        {component.disabled
                            ? <button onClick={e => this.props.onEnable(component.id)}>Enable</button>
                            : <button onClick={e => this.props.onDisable(component.id)}>Disable</button>
                        }
                    </Secure>
                </div>
                <br/>

                <Secure permission="component.edit" fallback={
                    <Label label={component.artifactId} altText={'no artifact id'}/>
                }>
                    <EditableLabel label={component.artifactId} altText={'no artifact id'}
                                   onLabelEdited={artifactId => this.props.onArtifactIdEdited(component.id, artifactId)}/>
                </Secure>
            </div>
        );
    }
}

export { ComponentBanner }