
import React from 'react';
import {Link} from "react-router-dom";
import "./layout.css"

class Clock extends React.Component {
    constructor(props) {
        super(props);
        this.state = {date: new Date()};

    }
    componentDidMount() {
        //console.info("mounted clock");
        this.tid = setInterval(() => this.tick(), 1000);
    }
    componentWillUnmount() {
        //console.info("unmounted clock");
        clearInterval(this.tid);
    }
    tick() {
        //console.info("tick");
        // setState to re-trigger a render
        this.setState({date: new Date()});
    }
    render() {
        return (
            <span>
                <b>{this.state.date.toLocaleDateString()} {this.state.date.toLocaleTimeString()}</b>
            </span>
        );
    }
}

class AppLayout extends React.Component {
    render() {
        return (
            <div className="container">
                <div className="header">
                    <div className="menu">
                        <h3 className="title">
                            {this.props.title}
                        </h3>
                        <span>
                            <Link to="/">index</Link> -&nbsp;
                            <Link to="/projects">projects</Link> -&nbsp;
                            <Link to="/envs">envs</Link> -&nbsp;
                            <Link to="/hosts">hosts</Link> -&nbsp;
                            <Link to="/archi">archi</Link> -&nbsp;
                            <Link to="/pipeline">pipeline</Link> -&nbsp;
                            <Link to="/sandbox">sandbox</Link> -&nbsp;
                            <Link to="/unknown">unknown page</Link>&nbsp;
                            <br/>
                        </span>
                        <span className="clock"><Clock /></span>
                    </div>
                </div>
                <div className="content">
                    <div className="content-wrapper">
                        {this.props.children}
                    </div>
                </div>
                <div className="footer">
                    Gomina!
                </div>
            </div>
        );
    }
}

class PrimarySecondaryLayout extends React.Component {
    render() {
        return (
            <div className='main-content'>
                <div className='principal-content'>
                    {this.props.children[0]}
                </div>
                <div className='side-content'>
                    <div className='side-content-wrapper'>
                        <div className='side-primary'>
                            {this.props.children[1]}
                        </div>
                        <div className='side-secondary'>
                            {this.props.children[2]}
                        </div>
                    </div>
                </div>

            </div>
        )
    }
}


export { AppLayout, PrimarySecondaryLayout };

