import React from 'react';
import {Link} from "react-router-dom";
import "./layout.css"
import Cookies from "js-cookie";
import {LoginForm} from "../../common/LoginForm";
import axios from "axios/index";
import {LoggedUserContext} from "../../permission/Secure"

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
    constructor(props) {
        super(props);
        this.state = {
            user: Cookies.get('gomina-user'),
            permissions: Cookies.get('gomina-permissions')
        }
    }
    login(username, password) {
        axios.post('/authenticate', {username: username, password: password})
            .then(response => {
                console.info("Logged in", response.data);
                // FIXME Cookies are not secure, implement a correct 'keep me logged in'
                Cookies.set('gomina-user', username);
                Cookies.set('gomina-permissions', response.data.permissions);
                this.setState({
                    'user': username,
                    'permissions': response.data.permissions
                })
            })
            .catch(function (error) {
                console.log("Login error", error.response);
            });
    }
    logout() {
        Cookies.remove('gomina-user');
        this.setState({'user': null})
    }
    render() {
        return (
            <LoggedUserContext.Provider value={{"user": this.state.user, "permissions": this.state.permissions}}>
                <div className="container">
                    <div className="header">
                        <div className="menu">
                            <h3 className="title">
                                {this.props.title}
                            </h3>
                            <span className="items">
                                <Link to="/">index</Link> -&nbsp;
                                <Link to="/components">components</Link> -&nbsp;
                                <Link to="/dependencies">dependencies</Link> -&nbsp;
                                <Link to="/architecture">architecture</Link> -&nbsp;
                                <Link to="/envs">envs</Link> -&nbsp;
                                <Link to="/hosts">hosts</Link> -&nbsp;
                                <Link to="/work">work</Link> -&nbsp;
                                <Link to="/pipeline">pipeline</Link> -&nbsp;
                                <Link to="/sandbox">sandbox</Link>
                                <br/>
                            </span>
                            <span className="context">
                                {this.state.user}
                                {this.state.user && <input type="button" onClick={e => this.logout()} value="Logout" />}
                                {!this.state.user && <LoginForm onAuthenticate={e => this.login(e.username, e.password)} />}

                                <Clock />
                            </span>
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
            </LoggedUserContext.Provider>
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


export { AppLayout, PrimarySecondaryLayout, Clock };

