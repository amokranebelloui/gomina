import React from "react";
import PropTypes from "prop-types";

class LoginForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {login: '', password: ''}
    }

    authenticate(e) {
        e.preventDefault();
        this.props.onAuthenticate && this.props.onAuthenticate({
            login: this.state.login,
            password: this.state.password
        });
    }

    render() {
        return (
            <form style={{display: 'inline-block'}} onSubmit={e => this.authenticate(e)}>
                <label>user</label>&nbsp;
                <input style={{width: '60px'}} name="login" type="text" defaultValue={this.state.login}
                       onChange={e => this.setState({login: e.target.value})} />
                &nbsp;
                <label>pass</label>&nbsp;
                <input style={{width: '60px'}} name="password" type="password"
                       onChange={e => this.setState({password: e.target.value})} />
                &nbsp;
                <input type="submit" value="Login"/>
            </form>
        )
    }
}

LoginForm.propTypes = {
    onAuthenticate: PropTypes.func.isRequired
};

export {LoginForm};
