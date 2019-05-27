// @flow
import * as React from "react"
import type {ComponentType} from "./ComponentType";
import {TagEditor} from "../common/TagEditor";
import {TagCloud} from "../common/TagCloud";
import {Secure} from "../permission/Secure";
import {EditableLabel} from "../common/EditableLabel";
import {Label} from "../common/Label";
import {EditableDate} from "../common/EditableDate";
import {DateTime} from "../common/DateTime";

type Props = {
    component: ComponentType,
    onTypeEdited: (componentId: string, type: string) => void,
    onInceptionDateEdited: (componentId: string, date: ?string) => void,
    onOwnerEdited: (componentId: string, owner: ?string) => void,
    onCriticityEdited: (componentId: string, criticity: ?string) => void,
    onSystemAdd: (componentId: string, system: string) => void,
    onSystemDelete: (componentId: string, system: string) => void,
    onLanguageAdd: (componentId: string, language: string) => void,
    onLanguageDelete: (componentId: string, language: string) => void,
    onTagAdd: (componentId: string, tags: string) => void,
    onTagDelete: (componentId: string, tags: string) => void,
}
type State = {

}

class ComponentMetadata extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const component = this.props.component;
        if (component && component.id) {
            return (
                <div className='component-badge'>
                    <b>Matadata: </b>
                    <br/>

                    <i>Type: </i>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <span>{component.type}</span>
                    }>
                        <EditableLabel label={component.type}
                                       onLabelEdited={t => this.props.onTypeEdited(component.id, t)}/>
                    </Secure>
                    <br/>

                    <i>Inception: </i>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <DateTime date={component.inceptionDate}  />
                    }>
                        <EditableDate date={component.inceptionDate}
                                      onDateEdited={inceptionDate => this.props.onInceptionDateEdited && this.props.onInceptionDateEdited(component.id, inceptionDate)} />
                    </Secure>
                    <br/>

                    <i>Owner: </i>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <Label label={component.owner}  />
                    }>
                        <EditableLabel label={component.owner}
                                       onLabelEdited={o => this.props.onOwnerEdited && this.props.onOwnerEdited(component.id, o)} />
                    </Secure>
                    <br/>

                    <i>Criticity: </i>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <Label label={component.criticity}  />
                    }>
                        <EditableLabel label={component.criticity}
                                       onLabelEdited={inceptionDate => this.props.onCriticityEdited && this.props.onCriticityEdited(component.id, inceptionDate)} />
                    </Secure>
                    <br/>

                    <i>Systems: </i>
                    <Secure permission="component.edit" fallback={
                        <TagCloud tags={component.systems} />
                    }>
                        <TagEditor tags={component.systems}
                                   onTagAdd={t => this.props.onSystemAdd(component.id, t)}
                                   onTagDelete={t => this.props.onSystemDelete(component.id, t)}
                        />
                    </Secure>
                    <br/>
                    <i>Languages: </i>
                    <Secure permission="component.edit" fallback={
                        <TagCloud tags={component.languages} />
                    }>
                        <TagEditor tags={component.languages}
                                   onTagAdd={t => this.props.onLanguageAdd(component.id, t)}
                                   onTagDelete={t => this.props.onLanguageDelete(component.id, t)}
                        />
                    </Secure>
                    <br/>
                    <i>Tags: </i>
                    <Secure permission="component.edit" fallback={
                        <TagCloud tags={component.tags} />
                    }>
                        <TagEditor tags={component.tags}
                                   onTagAdd={t => this.props.onTagAdd(component.id, t)}
                                   onTagDelete={t => this.props.onTagDelete(component.id, t)}
                        />
                    </Secure>
                </div>
            )
        }
        else {
            return (<div>Select a component to see details</div>)
        }
    }
}

export {ComponentMetadata}