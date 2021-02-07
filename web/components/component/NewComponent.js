// @flow
import * as React from "react"
import {ScmEditor} from "./ScmEditor";
import {SonarEditor} from "./SonarEditor";
import {BuildEditor} from "./BuildEditor";
import {Button, FormGroup, InputGroup, TagInput, Intent} from "@blueprintjs/core";

type NewComponentType = {
    id: string,
    label?: ?string,
    artifactId?: ?string,
    type?: ?string,
    systems: Array<string>,
    languages: Array<string>,
    tags: Array<string>,
    scmType?: ?string,
    scmUrl?: ?string,
    scmPath?: ?string,
    scmUsername?: ?string,
    scmPasswordAlias?: ?string,
    hasMetadata?: ?boolean,
    sonarServer?: ?string,
    jenkinsServer?: ?string,
    jenkinsJob?: ?string
}

type Props = {
    systemsContext?: Array<string>,
    buildServers?: Array<string>,
    sonarServers?: Array<string>,
    processing: boolean,
    error?: ?string,
    onAdd: NewComponentType => void,
    onCancel: () => void
}

class NewComponent extends React.Component<Props, NewComponentType> {
    constructor(props: Props) {
        super(props);
        this.state = {
            id: '',
            systems: this.props.systemsContext || [],
            languages: [],
            tags: []
        };
    }
    render() {
        return (
            <div>
                <FormGroup label="Label" labelInfo="(required)" labelFor="label-input" intent={Intent.PRIMARY}>
                    <InputGroup id="label-input" placeholder="Component Label"
                                onChange={e => this.setState({label: e.target.value})} />
                </FormGroup>

                <FormGroup label="ArtifactId" labelFor="artifactid-input">
                    <InputGroup id="artifactid-input" placeholder="Artifact Id"
                                onChange={e => this.setState({artifactId: e.target.value})} />
                </FormGroup>

                <FormGroup label="Type" labelFor="type-input">
                    <InputGroup id="type-input" placeholder="Type"
                                onChange={e => this.setState({type: e.target.value})} />
                </FormGroup>

                <FormGroup label="Systems" labelFor="systems-input">
                    <TagInput id="systems-input" placeholder="Systems" values={this.state.systems}
                              onChange={values => this.setState({systems: values})} />
                </FormGroup>

                <FormGroup label="Languages" labelFor="languages-input">
                    <TagInput id="languages-input" placeholder="Languages Used" values={this.state.languages}
                              onChange={values => this.setState({languages: values})} />
                </FormGroup>

                <FormGroup label="Tags" labelFor="tags-input">
                    <TagInput id="tags-input" placeholder="Tags" values={this.state.tags}
                              onChange={values => this.setState({tags: values})} />
                </FormGroup>

                <FormGroup label="SCM" labelFor="scm-input">
                    <ScmEditor onChanged={(type, url, path, md, u, p) => this.setState({
                        scmType: type,
                        scmUrl: url,
                        scmPath: path,
                        scmUsername: u,
                        scmPasswordAlias: p,
                        hasMetadata: md

                    })} />
                </FormGroup>

                <FormGroup label="Sonar" labelFor="sonar-input">
                    <SonarEditor servers={this.props.sonarServers || []} onChanged={(server) => this.setState({
                        sonarServer: server
                    })} />
                </FormGroup>

                <FormGroup label="Build" labelFor="build-input">
                    <BuildEditor servers={this.props.buildServers || []} onChanged={(server, job) => this.setState({
                        jenkinsServer: server,
                        jenkinsJob: job
                    })} />
                </FormGroup>

                <hr style={{marginTop: '4px', marginBottom: '4px'}} />
                {this.props.error &&
                    <span style={{color: 'red'}}><i>Error: {this.props.error}</i></span>
                }
                <Button text="Add" icon="add" onClick={e => this.props.onAdd(this.state)} disabled={this.props.processing} />
                <Button text="Cancel" icon="remove" onClick={e => this.props.onCancel()} />

                <br/>
                {JSON.stringify(this.state)}

            </div>
        )
    }
}


export { NewComponent }
export type { NewComponentType }
