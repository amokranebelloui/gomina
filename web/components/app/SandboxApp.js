import React from "react";
import {AppLayout} from "./common/layout";
import DatePicker from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css";
import axios from "axios/index"

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
