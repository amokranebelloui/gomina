import React from 'react';
import {storiesOf} from '@storybook/react';
import {MyComponent, Posts, Toggle2} from "../web/components/sandbox/sandbox";
import {DSM} from "../web/components/dependency/DSM";
import {DiffView} from "../web/components/diff/DiffView";
import {TextLines} from "../web/components/diff/TextLines";
import "../web/components/application.css"
import {Toggle} from "../web/components/common/Toggle";
import {action} from '@storybook/addon-actions';
import C1 from "../web/components/sandbox/module";
import {Clock} from "../web/components/app/common/layout";
import {CommitLog} from "../web/components/commitlog/CommitLog";
import {Documentation} from "../web/components/documentation/Documentation";
import {Badge} from "../web/components/common/Badge";
import {TagCloud} from "../web/components/common/TagCloud";
import {Tags} from "../web/components/common/Tags";
import {LoginForm} from "../web/components/common/LoginForm";
import {BuildStatus} from "../web/components/build/BuildStatus";
import {BuildLink} from "../web/components/build/BuildLink";
import {TagEditor} from "../web/components/common/TagEditor";
import {EditableLabel} from "../web/components/common/EditableLabel";
import {ScmEditor} from "../web/components/component/ScmEditor";
import {MonitoringEditor} from "../web/components/environment/MonitoringEditor";
import {ServiceModeEditor} from "../web/components/environment/ServiceModeEditor";
import {InlineAdd} from "../web/components/common/InlineAdd";
import * as axios from "axios";
import {AddEnvironment} from "../web/components/environment/AddEnvironment";
import {AddService} from "../web/components/environment/AddService";
import {AddInstance} from "../web/components/environment/AddInstance";
import {HostEditor} from "../web/components/environment/HostEditor";
import {HostConnectivityEditor} from "../web/components/environment/HostConnectivityEditor";
import {WorkEditor} from "../web/components/work/WorkEditor";
import {ApiDefinitionEditor} from "../web/components/component/ApiDefinitionEditor";
import {Autocomplete} from "../web/components/common/AutoComplete";

storiesOf('Components', module)
    .add('Clock', () => <Clock />)
    .add('Posts', () => {
        const posts = [
            {id: 1, title: 'Hello World 4', content: 'Welcome to learning React!'},
            {id: 2, title: 'Installation', content: 'You can install React from npm.'},
            {id: 3, title: 'Live Edit in Jetbrains, wo', content: 'You can live edit from Jetbrains IDE, wow!'},
            {id: 4, title: 'Scorpions, Tokyo Tapes', content: 'Kojo No Tsuki'}
        ];
        return (<Posts posts={posts}/>)
    })
    .add('Posts empty', () => {
        const posts = [];
        return (<Posts posts={posts}/>)
    })
    .add('Toggles', () => {
        return (
            <div>
                Toggles
                <Toggle toggled={true} onToggleChanged={action('toggle1')}/>
                <Toggle toggled={false} onToggleChanged={action('toggle2')}/>
            </div>
        )
    })
    .add('Toggle2', () => {
        return <Toggle2/>
    })
    .add('My Component', () => <MyComponent label="hello world" />)
    .add('My Component Highlighted', () => <MyComponent label="hello world" highlighted={true} />)
    .add('Test Default Export', () => <C1/>)
    .add('Badge with props', () =>
        <Badge title='Here' color="green" border='green' backgroundColor='lightgreen'>Here!<br/>and there</Badge>
    )
    .add('Badge with style overrides', () => <Badge title='Here' color="blue" style={{backgroundColor: 'orange', color: 'red', fontStyle: 'italic', fontWeight: 'bold', borderRadius: '9px'}}>Here!</Badge>)
    .add('Badge with selection', () =>
        [
            <Badge title='Clojure' value='clojure' onSelected={action('selected')}>Clojure</Badge>,
            <Badge title='Kotlin' value='kotlin' onSelected={action('selected')}>Kotlin</Badge>
        ]
    )
    .add('Tags', () => {
        const tags = [{value: "java", count: 13}, {value: ".net", count: 8}];
        return <Tags tags={tags} />
    })
    .add('TagCloud', () => {
        const tags = ["java", ".net", "java", "java", "kotlin", "javascript", "java"];
        return <TagCloud tags={tags} displayCount={true} selectionChanged={action("selectionChanged")} />
    })
    .add('TagEditor', () => {
        const tags = ["java", ".net", "kotlin", "javascript"];
        return <TagEditor tags={tags} displayCount={true} selectionChanged={action("selectionChanged")} />
    })
    .add('EditableLabel', () => {
        return (
            <EditableLabel label="Some Label" style={{fontSize: 30}} />
        )
    })
    .add('Autocomplete', () => {
        return (
            <div>
                <h4>Simple text</h4>
                <Autocomplete
                    suggestions={["java", "javascript", "c#", "c++", "kotlin", "go", "gherkin", "css", "python"]}
                    onChange={action('language change')} />
                <h4>Objects</h4>
                <Autocomplete suggestions={[
                    {id: 1, name: "java"},
                    {id: 2, name: "javascript"},
                    {id: 3, name: "c#"},
                    {id: 4, name: "c++"},
                    {id: 5, name: "kotlin"},
                    {id: 6, name: "go"},
                    {id: 7, name: "gherkin"},
                    {id: 8, name: "css"},
                    {id: 9, name: "python", version: "2"},
                    {id: 10, name: "python", version: "3"},
                ]} idProperty="id" labelProperty="name" onChange={action('language change {}')} />
            </div>
        )
    })

    .add('ScmEditor', () => {
        return (
            <ScmEditor onEdited={action('scmEdited')} onEditionCancelled={action('editionCancelled')} />
        )
    })
    .add('ApiEditor', () => {
        return (
            <div>
                <h4>Api</h4>
                <ApiDefinitionEditor type="api"
                                     onChange={action('api def changed')}
                                     onAdd={action('api def add')}
                                     onCancel={action('api def cancel')} />
                <h4>Usage</h4>
                <ApiDefinitionEditor type="usage"
                                     onChange={action('api usage changed')}
                                     onAdd={action('api usage add')}
                                     onCancel={action('api usage cancel')} />
            </div>
        )
    })
;

storiesOf('Dependency', module)
    .add('DSM', () => {
        const components = [
            "fixin",
            "rfq",
            "ioi",
            "posttrade",
            "order"
        ];
        const dependencies = [
            {from: "fixin", to: "fixin", count: 1, detail: "send RFQs"},
            {from: "fixin", to: "rfq", count: 1, detail: "send RFQs"},
            {from: "fixin", to: "order", count: 6, detail: "send orders"},
            {from: "order", to: "order", count: 4, detail: ""},
            {from: "ioi", to: "ioi", count: 4, detail: "internal"},
            {from: "posttrade", to: "order", count: 11, detail: "analyze orders"},
            {from: "posttrade", to: "posttrade", count: 11, detail: "analyze orders"},
            {from: "order", to: "posttrade", count: 11, detail: "analyze orders"}
        ];
        return (
            <DSM components={components} dependencies={dependencies} legend="true" />
        )
    });

storiesOf('CommitLog', module)
    .add('CommitLog', () => {
        const commits = [
            { revision: 45691, date: 1507987656241, author: 'amokrane', message: 'bug fix'},
            { revision: 45690, date: 1507187656241, author: 'amokrane', message: 'bug fix'},
            { revision: 45679, date: 1507177656241, author: 'amokrane', message: 'post release', version: '2.5.1-SNAPSHOT' },
            { revision: 45678, date: 1507177656241, author: 'amokrane', message: 'release', version: '2.5.0' },
            { revision: 45675, date: 1507077656241, author: 'amokrane', message: 'Implement using event sourcing' }
        ];
        const instances = [
            { id: 'uat-cart', env: 'uat', name: 'cart', deployVersion: '', deployRevision: '', version: '2.5.1-SNAPSHOT', revision: '45680' },
            { id: 'uat-cart', env: 'uat', name: 'cart2', deployVersion: '2.5.1-SNAPSHOT', deployRevision: '' },
            { id: 'stg-cart', env: 'staging', name: 'cart' },
            { id: 'prod-cart', env: 'prod', name: 'cart', deployVersion: '', deployRevision: '', version: '2.5.0', revision: '45678' }
        ];
        return (
            <CommitLog commits={commits} instances={instances} />
        )
    });

storiesOf('Build', module)
    .add('Build Link', () => {
        return [
            <BuildLink server="home" job="mylib" />,
            <BuildLink server="home" job="mylib" url="http://jenkins.io" />,
            <BuildLink server="home" />,
            <BuildLink job="mylib"/>,
            <BuildLink/>,
        ]
    })
    .add('Build Status', () => {
        return [
            <BuildStatus status="SUCCESS"/>,
            <BuildStatus status="success"/>,
            <BuildStatus status="FAILURE"/>,
            <BuildStatus status="UNKNOWN"/>,
            <BuildStatus/>,
        ]
    })
;
storiesOf('Documentation', module)
    .add('Documentation', () => {
        const doc = `<h1>Sample documentation</h1>
description of the component
<h2>Features</h2>
<li>Handling the cart</li>
<li>analysis of why users remove articles from cart</li>
<h3>Resiliency characteristics</h3>
description
<br/>
<br/>
<h3>Dependencies</h3>
<li>database</li>
`;
        return (
            <Documentation doc={doc} />
        )
    });




storiesOf('Environment', module)
    .add('InlineEditor', () => {
        let envId;
        let envName;
        const addEnv = () => {
            console.info("AAA");
            return axios.post('http://localhost:8080/data/envs/add?envId=' + envId, envName);
        };
        return (
            <InlineAdd type="Environment"
                       action={() => addEnv()}
                       onItemAdded={action('added')}
                       onAddError={action('error')}
                       editionForm={() =>
                           <div style={{border: 'solid 1px red'}}>
                               <span>Add Env</span>
                               <input type="text" name="id" placeholder="env Id" onChange={e => envId = e.target.value}/>
                               <input type="text" name="name" placeholder="env Name" onChange={e => envName = e.target.value}/>
                           </div>
                       }
                       successDisplay={data =>
                          <div style={{border: 'solid 1px red'}}>
                            <b>{data ? JSON.stringify(data) : 'no data returned'}</b>
                          </div>
                      }
            />
        );
    })
    .add('AddEnvironment', () =>
        <AddEnvironment onEnvironmentAdded={action('added')} />
    )
    .add('AddService', () =>
        <AddService env="UAT" onServiceAdded={action('added')} />
    )
    .add('AddInstance', () =>
        <AddInstance env="UAT" svc="order" onInstanceAdded={action('added')} />
    )
    .add('MonitoringEditor', () =>
        <MonitoringEditor url="tc://localhost:1456" id="UAT"
                          onChanged={action('changed')}
                          onEditionCancelled={action('cancelled')}
                          onEdited={action('edited')} />)
    .add('ServiceModeEditor', () =>
        <ServiceModeEditor mode="LEADERSHIP" count="3"
                          onChanged={action('changed')}
                          onEditionCancelled={action('cancelled')}
                          onEdited={action('edited')} />)
    .add('HostEditor', () =>
        <HostEditor host={{host: "localhost", dataCenter: "DC", type: "TEST"}}
                           onChange={action('changed')} />)
    .add('HostConnectivityEditor', () =>
        <HostConnectivityEditor host={{host: "localhost", username: "john.doe", passwordAlias: "@jd"}}
                    onChange={action('changed')} />)
;


storiesOf('Work', module)
    .add('WorkEditor', () =>
        <WorkEditor work={{host: "localhost", dataCenter: "DC", type: "TEST"}}
                    onChange={action('changed')} />)

storiesOf('Text', module)
    .add('Diff', () => {
        const config = `# Sample configuration
# to display

# First property dsdfs sdfzsd  sfsdd sfserfdfq sd sf dfsdfser
prop1 = true
# AUTO, MANUAL                
prop2 = MANUAL

some.action.enabled=true`;
        const config2 = `# Sample configuration to display

# First property dsdfs sdfzsd  sfsdd sfserfdfq sd sf dfsdfser
prop1 = true
# AUTO, MANUAL                
prop2 = AUTO

some.other.action.enabled=true
some.action.enabled=true

timeout=12

`;
        return <DiffView left={config} right={config2} format="properties" />
    })
    .add('TextLines', () => {
        const text = `# Sample configuration to display

# First property dsdfs sdfzsd  sfsdd sfserfdfq sd sf dfsdfser
prop1 = true
# AUTO, MANUAL                
prop2 = AUTO

some.other.action.enabled=true
some.action.enabled=true

timeout=12

`;
        let highlights = [
            {num: 1, highlight: 'removed'},
            {num: 6, highlight: 'modified'},
            {num: 9, highlight: 'added'},
            {num: 11, highlight: 'added'}
        ];
        function highlightFor(num) {
            const line = highlights && highlights.find(h => h.num === num);
            return line ? line.highlight : null
        }
        const lines = text.split('\n').map((line, i) => {
            return {num: i+1, text: line, highlight: highlightFor(i + 1)}
        });
        return <TextLines lines={lines} />
    });

storiesOf('Layout', module)
    .add('Login Form', () => {
        return (
            <LoginForm onAuthenticate={action("authenticate")} />
        )
    })
    .add('Containers', () => {
        return (
            <div>
                <div style={{height: '100px', display: 'table', border: '1px red solid'}}>
                    <span style={{display: 'table-cell', verticalAlign: 'middle'}}>ooh text ::
                    <br/>
                    something else
                    </span>
                </div>


                <div style={{border: 'red 1px solid'}}>
                    sdsfd<br/>
                    sqdf<br/>
                    <span style={{lineHeight: '1.8', verticalAlign: 'top'}}>ksdhdg</span>
                </div>

                <div>
                    <span>
                        <span style={{display: 'block'}}>Main text</span>
                        <span style={{fontSize: 7, color: 'gray', lineHeight: '40%'}}>Tagline</span>
                    </span>
                </div>

                <div>

                    <div style={{display: 'table-cell', height: '200px', width: '300px', border: '1px solid red'}}>
                        <div style={{display: 'block-table', height: '100%', backgroundColor: 'yellow'}}>
                            hello world
                        </div>
                    </div>

                </div>

                <div>
                    <div style={{display: 'table', width: '100px', border: '1px solid blue'}}>
                        <div style={{display: 'inline-block', backgroundColor: 'orange', width: '500px'}}> Something</div>

                    </div>
                </div>
            </div>
        )
    });