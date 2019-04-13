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

                <DatePicker
                    dateFormat="dd/MM/yyyy"
                    selected={this.state.startDate}
                    onChange={date => this.changeDate(date)}
                />
                <br/>
                <b>Date: </b>{JSON.stringify(this.state.startDate)}<br/>
                <b>Date: </b>{JSON.stringify(this.state.startDate && this.state.startDate.getFullYear())}<br/>
                <b>Month: </b>{JSON.stringify(this.state.startDate && this.state.startDate.getMonth())}<br/>
                <b>Day: </b>{JSON.stringify(this.state.startDate && this.state.startDate.getDay())}<br/>

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
