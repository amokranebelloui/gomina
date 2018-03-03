

import React from 'react';
import PropTypes from 'proptypes';

function Clock1(props) {
    return (
        <span>
                <b>It's {props.date.toLocaleTimeString()}.</b>
            </span>
    )
}

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

class LoggingButton extends React.Component {

    handleClick() {
        console.log('this is:', this);
    }

    render() {
        return (
            <button onClick={e => this.handleClick(e)}>
                Click me
            </button>
        );
    }
}

function WarningBanner(props) {
    if (!props.warn) {
        return null;
    }

    return (
        <div className="warning">
            Warning! {props.warn}
        </div>
    );
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

export {MyComponent, Clock1, Toggle2, LoggingButton, Posts, WarningBanner}
