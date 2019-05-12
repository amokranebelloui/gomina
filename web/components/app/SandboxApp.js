import React from "react";
import {AppLayout} from "./common/layout";
import DatePicker from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css";
import axios from "axios/index"
import "../common/common.css"

class SandboxApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            startDate: new Date()
        }
    }

    render() {
        return (
            <AppLayout title={'Sandbox'}>
                <h3>Sandbox</h3>

                <button onClick={e => axios.post('/data/user/add?userId=lc3354',
                    {login: 'linus_c', shortName: 'Linus', firstName: 'Linus', lastName: 'Crusoé', accounts: []})}
                >
                    Create Linus
                </button><br/>

                <button onClick={e => axios.put('/data/user/jd4436/update', {shortName: 'John D', firstName: 'John', lastName: 'Doe', accounts: ['johnd']})}>Update -> John D</button><br/>
                <button onClick={e => axios.put('/data/user/jd4436/update', {shortName: 'Johnny', firstName: 'Jonathan', lastName: 'Dow', accounts: ['johnd']})}>Update -> Johnny</button><br/>

                <div className='clearfix' style={{display: 'table', position: 'relative', width: '100%', border: '1px solid green'}}>
                    <div style={{display: 'table-cell', border: '1px solid blue'}}>
                        AAA <br/>2
                        BBB <br/>
                        CCC <br/>
                    </div>
                    <div style={{position: 'absolute', height: '100%', top: '0px', bottom: '0px', right: '0px', float: 'right', border: '1px solid red'}}>
                        BBB
                    </div>
                    <div style={{clear: 'both'}}>&nbsp;</div>
                </div>

            </AppLayout>
        );
    }

    changeDate(date) {
        console.log(date);
        console.log(new Date(date));
        this.setState({startDate: new Date(date)});
        axios.put("/date", {date: date, date2: new Date(date)})
    }
}

export {SandboxApp};
