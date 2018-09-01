import React from "react";
import './DSM.css'

class DSM extends React.Component {
    constructor(props) {
        super(props);
        this.state = {selectedComponents: [], selectedDependencies: []};
        //this.onComponentClicked = this.onComponentClicked.bind(this); // This binding is necessary to make `this` work in the callback
    }

    isComponentSelected(comp) {
        return this.state.selectedComponents.indexOf(comp) !== -1;
    }
    statusToSelectedComponent(comp) {
        let uses = this.props.dependencies.find(d => d.from !== d.to && d.from === comp && this.state.selectedComponents.includes(d.to));
        let used = this.props.dependencies.find(d => d.from !== d.to && d.to === comp && this.state.selectedComponents.includes(d.from));
        let internal = this.props.dependencies.find(d => d.from === d.to && d.from == comp && d.to == comp && this.state.selectedComponents.includes(comp));
        if (uses && used) {
            return "cycle" + (internal ? " internal" : "")
        }
        else if (uses) {
            return "uses" + (internal ? " internal" : "")
        }
        else if (used) {
            return "used" + (internal ? " internal" : "")
        }
        return internal && "internal"
    }

    isDependencySelected(from, to) {
        return this.state.selectedDependencies.find(dep => dep.from === from && dep.to === to) && true;
    }
    statusToSelectedDependencies(from, to) {
        if (this.state.selectedDependencies.find(d => to === d.from)) {
            return "uses"
        }
        else if (this.state.selectedDependencies.find(d => from === d.to)) {
            return "used"
        }
        /**/
        else if (this.state.selectedDependencies.find(d => from === d.from)) {
            return "uses"
        }
        else if (this.state.selectedDependencies.find(d => to === d.to)) {
            return "used"
        }
        /**/
        return "none";
    }

    checked(from, to) {
        const d = this.props.dependencies.find(d => d.from === from && d.to === to);
        const count = d ? d.count : null;
        return count
    }
    onComponentClicked(comp, e) {
        console.log('comp', comp);
        let newList;
        if (e.metaKey || e.ctrlKey) {
            if (this.isComponentSelected(comp)) {
                newList = this.state.selectedComponents.filter(i => i !== comp)
            }
            else {
                newList = this.state.selectedComponents.concat([comp])
            }
        }
        else {
            newList =  [comp]
        }
        this.setState({selectedComponents: newList})
    }
    onDependencyClicked(from, to, e) {
        const dep = {from: from, to: to};
        console.log('dep', from, to, dep, e.metaKey, e.altKey);
        let newList;
        if (e.metaKey || e.ctrlKey) {
            if (this.isDependencySelected(from, to)) {
                newList = this.state.selectedDependencies.filter(i => i.from !== dep.from || i.to !== dep.to)
            }
            else {
                newList = this.state.selectedDependencies.concat([dep])
            }
        }
        else {
            newList = [dep]
        }
        this.setState({selectedDependencies: newList})
    }
    render() {
        return (
            <div className="dsm">
                <table className="dsm-table">
                    <tbody>
                    <tr key="head">
                        <td colSpan={2}></td>
                        {this.props.components.map(comp =>
                        <td key={comp} className="dsm-header" align="center" valign="bottom">
                            <span className="dsm-header-label"><b>{comp}</b></span>
                        </td>
                        )}
                    </tr>
                    {this.props.components.map(to =>
                        <tr key={to}>
                            <DSMComponent
                                comp={to}
                                selected={this.isComponentSelected(to)}
                                status={this.statusToSelectedComponent(to)}
                                onClick={e => this.onComponentClicked(to, e)} />
                            {this.props.components.map(from =>
                                <DSMCell
                                    key={from + '->' + to}
                                    from={from}
                                    to={to}
                                    count={this.checked(from, to)}
                                    selected={this.isDependencySelected(from, to)}
                                    status={this.statusToSelectedDependencies(from, to)}
                                    onClick={e => this.onDependencyClicked(from, to, e)}/>
                            )}
                        </tr>
                    )}
                    </tbody>
                </table>
                {this.props.legend === "true" && <DSMLegend/>}
            </div>
        );
    }
}

function DSMComponent(props) {
    let clazz = "dsm-component";
    if (props.selected) {
        clazz = clazz + " selected"
    }

    let depClazz = "dsm-component";
    if (props.status) {
        depClazz = depClazz + " " + props.status
    }
    return ([
        <td key="component" align="right"
            className={clazz}
            onClick={e => props.onClick && props.onClick(e)}>
            <b>{props.comp}</b>
        </td>,
        <td key="depStatus" style={{width: '3px'}} className={depClazz}></td>,
    ])
}

function DSMCell(props) {
    let clazz = "dsm-cell";

    // Selection
    if (props.selected) {
        clazz = clazz + " selected"
    }
    else if (props.status) {
        clazz = clazz + " " + props.status
    }

    // Global
    if (props.count) {
        clazz = clazz + " dep";
    }
    else {
        clazz = clazz + " nodep";
    }
    if (props.from === props.to) {
        clazz = clazz + " self"
    }
    //style={{cursor: props.count && 'pointer'}}
    return (
        <td className={clazz}
            onClick={e => props.onClick && props.onClick(e)}>
            {props.count}
        </td>
    )
}


function DSMLegend(props) {
    return (
        <table>
            <tbody>
            <tr>
                <td className="dsm-component uses"
                    title="Uses selected components">
                    uses
                </td>
                <td className="dsm-component uses internal"
                    title="">
                    uses<br/>internal
                </td>
                <td className="dsm-component used">
                    used
                </td>
                <td className="dsm-component used internal">
                    used<br/>internal
                </td>
                <td className="dsm-component cycle">
                    cycle
                </td>
                <td className="dsm-component cycle internal">
                    cycle<br/>internal
                </td>
                <td className="dsm-component internal">
                    internal
                </td>
            </tr>
            </tbody>
        </table>
    )
}

export { DSM }