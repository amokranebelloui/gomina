import React from "react";

class Toggle extends React.Component {
    constructor(props) {
        super(props);
        //this.state = {isToggleOn: props.toggled};

        // This binding is necessary to make `this` work in the callback
        //this.toggle = this.toggle.bind(this);
    }

    toggle(e) {
        /*
         this.setState(prevState => ({
         isToggleOn: !prevState.isToggleOn
         }));
         */
        //this.setState({isToggleOn: e})
        if (this.props.onToggleChanged) {
            this.props.onToggleChanged(e)
        }
    }

    render() {
        return (
            <button onClick={e => this.toggle(!this.props.toggled)}>
                {this.props.toggled ? 'ON' : 'OFF'}
            </button>
        );
    }
}

export {Toggle}