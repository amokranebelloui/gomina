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
            userId: Cookies.get('gomina-userId'),
            login: Cookies.get('gomina-login'),
            permissions: Cookies.get('gomina-permissions')
        }
    }
    login(login, password) {
        console.info("Authenticating", login);
        axios.post('/authenticate', {login: login, password: password})
            .then(response => {
                console.info("Logged in", response.data);
                // FIXME Cookies are not secure, implement a correct 'keep me logged in'
                Cookies.set('gomina-userId', response.data.userId);
                Cookies.set('gomina-login', login);
                Cookies.set('gomina-permissions', response.data.permissions);
                this.setState({
                    'userId': response.data.userId,
                    'login': login,
                    'permissions': response.data.permissions
                });
                this.props.onUserLoggedIn && this.props.onUserLoggedIn(response.data.userId)
            })
            .catch(function (error) {
                console.log("Login error", error.response);
            });
    }
    logout() {
        Cookies.remove('gomina-userId');
        Cookies.remove('gomina-login');
        Cookies.remove('gomina-permissions');
        this.setState({'userId': null, 'login': null, 'permissions': null})
    }
    render() {
        return (
            <LoggedUserContext.Provider value={{"userId": this.state.userId, "login": this.state.login, "permissions": this.state.permissions}}>
                <div className="container">
                    <div className="header">
                        <div className="menu">
                            <h3 className="title">
                                {this.props.title}
                            </h3>
                            <span className="items">
                                <Link to="/">index</Link> &nbsp;
                                <Link to="/components">components</Link> &nbsp;
                                <Link to="/libraries">libraries</Link> &nbsp;
                                <Link to="/dependencies">dependencies</Link> &nbsp;
                                <Link to="/architecture">architecture</Link> &nbsp;
                                <Link to="/envs">envs</Link> &nbsp;
                                <Link to="/hosts">hosts</Link> &nbsp;
                                <Link to="/work">work</Link> &nbsp;
                                <Link to="/user">users</Link> &nbsp;
                                <Link to="/pipeline">pipeline</Link> &nbsp;
                                <Link to="/sandbox">sandbox</Link>
                                <br/>
                            </span>
                            <span className="context">
                                {this.state.userId} {this.state.login && '-'} {this.state.login}
                                {this.state.userId && <input type="button" onClick={e => this.logout()} value="Logout" />}
                                {!this.state.userId && <LoginForm onAuthenticate={e => this.login(e.login, e.password)} />}

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

