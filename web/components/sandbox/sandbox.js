import React from 'react';
import PropTypes from 'proptypes';

class MyComponent extends React.Component {
    render() {
        let text = "'" + this.props.label + "'";
        if (this.props.highlighted) {
            text = <b> {text} </b>;
        }
        return (
            <span>Special {text}</span>
        )
    }
}
MyComponent.propTypes = {
    label: PropTypes.string.isRequired,
    highlighted: PropTypes.bool
};

class Toggle2 extends React.Component {
    constructor(props) {
        super(props);
        this.state = {isToggleOn: true};
        this.toggle = this.toggle.bind(this); // This binding is necessary to make `this` work in the callback
    }
    toggle() {
        this.setState(prevState => ({
            isToggleOn: !prevState.isToggleOn
        }));
    }
    render() {
        return (
            <button onClick={this.toggle}>
                {this.state.isToggleOn ? 'ON' : 'OFF'}
            </button>
        );
    }
}


class Posts extends React.Component {
    constructor(props) {
        super(props);
        //this.state = {logged: false, user: "amo"}
    }
    render() {
        return (
            <ul>
                {this.props.posts.map((post) =>
                    <li key={post.id}>{post.title}</li>
                )}
            </ul>
        )
    }
}

export {MyComponent, Toggle2, Posts}
