import React from "react";
import {LoggingButton, MyComponent, Posts, Toggle2, WarningBanner} from "../sandbox/sandbox";
import C1 from "../sandbox/module";
import {Toggle} from "../common/component-library";
import {AppLayout} from "./common/layout";
import {DSM} from "../dependency/DSM";

class SandboxApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {logged: true, user: "amo"}
    }

    render() {
        const posts = [
            {id: 1, title: 'Hello World 4', content: 'Welcome to learning React!'},
            {id: 2, title: 'Installation', content: 'You can install React from npm.'},
            {id: 3, title: 'Live Edit in Jetbrains, wo', content: 'You can live edit from Jetbrains IDE, wow!'},
            {id: 4, title: 'Scorpions, Tokyo Tapes', content: 'Kojo No Tsuki'}
        ];

        let status;
        if (this.state.logged) {
            status = <span>Hello '{this.state.user}'</span>
        }
        else {
            status = <span>Hi buddy</span>
        }

        const components = [
            "fixin",
            "rfq",
            "posttrade",
            "order"
        ];
        const dependencies = [
            {from: "fixin", to: "rfq", count: 1, detail: "send RFQs"},
            {from: "fixin", to: "order", count: 6, detail: "send orders"},
            {from: "order", to: "order", count: 4, detail: ""},
            {from: "posttrade", to: "order", count: 11, detail: "analyze orders"},
            {from: "order", to: "posttrade", count: 11, detail: "analyze orders"}
        ];

        return (
            <AppLayout title='Sandbox'>
                <span>{status}</span>&nbsp;
                {this.state.logged && <span>Logged in</span>}
                <br/>

                <DSM components={components} dependencies={dependencies} />

                Linked toggles
                <Toggle toggled={this.state.logged} onToggleChanged={e => {
                    this.setState({logged: e})
                }}/>
                <Toggle toggled={this.state.logged} onToggleChanged={e => {
                    this.setState({logged: e})
                }}/>
                <br/>
                Independent <Toggle2/>
                <C1/>
                <LoggingButton/>
                <WarningBanner warn="Something happened"/>
                <WarningBanner warn=""/>
                <br/>
                <Posts posts={posts}/>
                <MyComponent label="hello world" highlighted={true}/>
                <MyComponent label="hello world"/>

                <hr/>

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

            </AppLayout>
        );
    }
}

export {SandboxApp};
