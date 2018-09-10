import React from 'react';
import {storiesOf} from '@storybook/react';
import {MyComponent, Posts, Toggle2} from "../web/components/sandbox/sandbox";
import {DSM} from "../web/components/dependency/DSM";
import {DiffView} from "../web/components/diff/DiffView";
import {TextLines} from "../web/components/diff/TextLines";
import "../web/components/application.css"
import {Toggle} from "../web/components/common/Toggle";
import { action } from '@storybook/addon-actions';
import C1 from "../web/components/sandbox/module";
import {Clock} from "../web/components/app/common/layout";
import {CommitLog} from "../web/components/commitlog/CommitLog";

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