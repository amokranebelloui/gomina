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
    doesComponentUsesSelected(comp) {
        return this.props.dependencies.find(d => d.from === comp && this.state.selectedComponents.includes(d.to));
    }
    isComponentUsedBySelected(comp) {
        return this.props.dependencies.find(d =>  this.state.selectedComponents.includes(d.from) && d.to === comp);
    }
    isDependencySelected(from, to) {
        return this.state.selectedDependencies.find(dep => dep.from === from && dep.to === to) && true;
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
        // Selected {this.state.selectedDependencies.map(i => <li>{i}</li>)}
        return (
            <div>
                DSM<br/>
                <table className="dsm-table">
                    <tr>
                        <td colSpan={2}></td>
                        {this.props.components.map(comp =>
                        <td className="dsm-header" align="center" valign="bottom">
                            <span className="dsm-header-label"><b>{comp}</b></span>
                        </td>
                        )}
                    </tr>
                    {this.props.components.map(to =>
                        <tr>
                            <DSMComponent comp={to}
                                          selected={this.isComponentSelected(to)}
                                          usesSelected={this.doesComponentUsesSelected(to)}
                                          usedBySelected={this.isComponentUsedBySelected(to)}
                                          onClick={e => this.onComponentClicked(to, e)} />
                            {this.props.components.map(from =>
                                <DSMCell
                                    from={from}
                                    to={to}
                                    count={this.checked(from, to)}
                                    selected={this.isDependencySelected(from, to)}
                                    onClick={e => this.onDependencyClicked(from, to, e)}/>
                            )}
                        </tr>
                    )}
                </table>
                Selected {this.state.selectedDependencies.map(i => <li>{i.from} -> {i.to}</li>)}
            </div>
        );
    }
}

function DSMComponent(props) {
    let clazz = "dsm-component";
    if (props.selected) {
        clazz = clazz + " selected"
    }
    let color = null;
    if (props.usesSelected && props.usedBySelected && props.selected) {
        color = 'orange'
    }
    else if (props.usesSelected && props.usedBySelected) {
        color = 'red'
    }
    else if (props.usesSelected) {
        color = 'green'
    }
    else if (props.usedBySelected) {
        color = 'goldenrod'
    }
    return ([
        <td align="right"
            className={clazz}
            onClick={e => props.onClick && props.onClick(e)}>
            <b>{props.comp}</b>
        </td>,
        <td style={{width: '3px', backgroundColor: color}} className="dsm-component"></td>,
    ])
}

function DSMCell(props) {
    let clazz = "dsm-cell";
    if (props.count) {
        clazz = clazz + " dsm-cell-dep";
    }
    else {
        clazz = clazz + " dsm-cell-nodep";
    }
    if (props.from === props.to) {
        clazz = clazz + " dsm-cell-self"
    }
    if (props.selected) {
        clazz = clazz + " selected"
    }
    //style={{cursor: props.count && 'pointer'}}
    return (
        <td className={clazz}
            onClick={e => props.onClick && props.onClick(e)}>
            {props.count}
        </td>
    )
}


export { DSM }