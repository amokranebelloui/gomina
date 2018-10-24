import React from "react";

class Coverage extends React.Component {
    render() {
        const base = {padding: '2px', borderRadius: '3px', display: 'block'};
        const high = {width: 110, backgroundColor: 'green', color: 'white'};
        const medium = {width: 80, backgroundColor: 'orange', color: 'white'};
        const low = {width: 50, backgroundColor: 'red', color: 'white'};
        const unknown = {width: 50, backgroundColor: '#lightgray', color: 'gray'};

        const coverage = this.props.coverage;
        const style = coverage > 20 ? high : coverage > 8 ? medium : coverage > 0 ? low : unknown;
        return (
            <span style={{display: 'inline-block', width: 114, backgroundColor: '#E8E8E8', borderRadius: '3px'}}>
                    <span style={{float: 'right', color: '#BBBBBB', fontSize: 10}}>
                        (22/24)
                    </span>
                    <span style={Object.assign(base, style)}>
                        {coverage ? coverage + ' %' : 'Unknown'}
                    </span>
                </span>
        );
    }
}

export {Coverage}
