import React from "react";
import './DSM.css'

class DSM extends React.Component {
    constructor(props) {
        super(props);
        this.state = {selectedComponents: [], selectedDependencies: []};
        //this.onComponentClicked = this.onComponentClicked.bind(this); // This binding is necessary to make `this` work in the callback
    }

    selectComponent(comp, multi) {
        let newList;
        if (multi) {
            if (this.isComponentSelected(comp)) {
                newList = this.state.selectedComponents.filter(i => i !== comp)
            }
            else {
                newList = this.state.selectedComponents.concat([comp])
            }
        }
        else {
            newList = [comp]
        }
        this.setState({selectedComponents: newList})
    }
    isComponentSelected(comp) {
        const selected =
            this.state.selectedComponents.indexOf(comp) !== -1 ||
            //this.state.selectedComponents.includes(d.to) ||
            this.state.selectedDependencies.find(d => d.from === comp || d.to === comp);
        return selected;
    }
    statusToSelectedComponent(comp) {
        let uses = this.props.dependencies.find(d => d.from !== d.to && d.from === comp && this.isComponentSelected(d.to));
        let used = this.props.dependencies.find(d => d.from !== d.to && d.to === comp && this.isComponentSelected(d.from));
        let internal = this.props.dependencies.find(d => d.from === d.to && d.from == comp && d.to == comp && this.isComponentSelected(comp));
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

    getDependency(from, to) {
        return this.props.dependencies.find(d => d.from === from && d.to === to);
    }
    selectDependency(from, to, multi) {
        const dep = {from: from, to: to};
        let newList;
        if (multi) {
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
    
    onComponentClicked(comp, e) {
        console.log('comp', comp);
        this.selectComponent(comp, e.metaKey || e.ctrlKey);
    }
    onDependencyClicked(from, to, e) {
        console.log('dep', from, to, e.metaKey, e.altKey);
        this.selectDependency(from, to, e.metaKey || e.ctrlKey);
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
                                    dependency={this.getDependency(from, to)}
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
        <td key="depStatus" style={{width: '3px'}} className={depClazz}>&nbsp;</td>,
    ])
}

function DSMCell(props) {
    const d = props.dependency;
    
    let clazz = "dsm-cell";

    // Selection
    if (props.selected) {
        clazz = clazz + " selected"
    }
    else if (props.status) {
        clazz = clazz + " " + props.status
    }

    // Global
    if (d) {
        clazz = clazz + " dep";
    }
    else {
        clazz = clazz + " nodep";
    }
    if (props.from === props.to) {
        clazz = clazz + " self"
    }
    return (
        <td className={clazz} title={d && d.detail}
            onClick={e => props.onClick && props.onClick(e)}>
            {d && d.count}
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