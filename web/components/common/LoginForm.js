import React from "react";
import PropTypes from "prop-types";

class LoginForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {username: 'amokrane', password: ''}
    }

    authenticate(e) {
        e.preventDefault();
        this.props.onAuthenticate && this.props.onAuthenticate({
            username: this.state.username,
            password: this.state.password
        });
    }

    render() {
        return (
            <form style={{display: 'inline-block'}} onSubmit={e => this.authenticate(e)}>
                <label>user</label>&nbsp;
                <input style={{width: '60px'}} name="username" type="text" defaultValue={this.state.username}
                       onChange={e => this.setState({username: e.target.value})} />
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
