import React from "react";

class Documentation extends React.Component {
    // FIXME Initial values?
    componentWillReceiveProps(nextProps) { // Only on changes
        this.content.innerHTML = nextProps.doc;
    }

    render() {
        return (
            <div>
                <b>Doc</b>
                <br/>
                <div ref={(ref) => {
                    this.content = ref;
                }}></div>
            </div>
        )
    }
}

export {Documentation}
