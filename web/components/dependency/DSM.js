import React from "react";
import './DSM.css'
import PropTypes from 'prop-types'

const outOfScopeOpacity = 0.35;

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
    unselectComponents() {
        this.setState({selectedComponents: []})
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
        this.setState({selectedDependencies: newList});
        this.props.onSelectedDependenciesChanged && this.props.onSelectedDependenciesChanged(newList)
    }
    unselectDependencies() {
        this.setState({selectedDependencies: []});
        this.props.onSelectedDependenciesChanged && this.props.onSelectedDependenciesChanged([])
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
        //console.info("DSM click", comp, e.metaKey, e.ctrlKey);
        let multi = e.metaKey || e.ctrlKey;
        !multi && this.unselectDependencies();
        this.selectComponent(comp, multi);
    }
    onDependencyClicked(from, to, e) {
        //console.info("DSM click", from, to, e.metaKey, e.ctrlKey);
        let multi = e.metaKey || e.ctrlKey;
        !multi && this.unselectComponents();
        this.selectDependency(from, to, multi);
    }
    
    render() {
        const components = this.props.components || [];
        return (
            <div className="dsm">
                <table className="dsm-table">
                    <tbody>
                    <tr key="head">
                        <td colSpan={2}></td>
                        {components.map(comp =>
                        <td key={comp.serviceId} className="dsm-header"
                            align="center" valign="bottom"
                            style={{opacity: (comp.inScope ? null : outOfScopeOpacity)}}>
                            <span className="dsm-header-label">
                                <b><span style={{whiteSpace: 'nowrap'}}>{comp.serviceId}</span></b>
                            </span>
                        </td>
                        )}
                    </tr>
                    {components.map((to, j) =>
                        <tr key={to.serviceId}>
                            <DSMComponent
                                comp={to.serviceId}
                                selected={this.isComponentSelected(to.serviceId)}
                                status={this.statusToSelectedComponent(to.serviceId)}
                                onClick={e => this.onComponentClicked(to.serviceId, e)}
                                inScope={to.inScope}
                            />
                            {components.map((from, i) =>
                                <DSMCell
                                    key={from.serviceId + '->' + to.serviceId}
                                    from={from.serviceId}
                                    to={to.serviceId}
                                    dependency={this.getDependency(from.serviceId, to.serviceId)}
                                    cycle={(i > j)}
                                    selected={this.isDependencySelected(from.serviceId, to.serviceId)}
                                    status={this.statusToSelectedDependencies(from.serviceId, to.serviceId)}
                                    onClick={e => this.onDependencyClicked(from.serviceId, to.serviceId, e)}
                                    inScope={to.inScope && from.inScope}
                                />
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
            style={{opacity: (props.inScope ? null : outOfScopeOpacity)}}
            onClick={e => props.onClick && props.onClick(e)}>
            <b><span style={{whiteSpace: 'nowrap'}}>{props.comp}</span></b>
        </td>,
        <td key="depStatus" style={{width: '3px', opacity: (props.inScope ? null : outOfScopeOpacity)}} className={depClazz}>&nbsp;</td>,
    ])
}

function DSMCell(props) {
    const d = props.dependency;
    const c = props.cycle;

    let clazz = "dsm-cell";

    // Selection
    if (props.selected) {
        clazz = clazz + " selected"
    }
    else if (props.status) {
        clazz = clazz + " " + props.status
    }

    // Global
    if (c) {
        if (d) {
            clazz = clazz + " cycle";
        }
    }
    else {
        if (d) {
            clazz = clazz + " dep";
        }
        if (props.from === props.to) {
            clazz = clazz + " self"
        }
    }

    return (
        <td className={clazz} title={d && d.detail}
            onClick={e => props.onClick && props.onClick(e)}
            style={{opacity: (props.inScope ? null : outOfScopeOpacity)}}>
            {d && d.count}
        </td>
    )
}

DSM.propTypes = {
    "components": PropTypes.array.isRequired, // {service: serviceId, inScope: boolean}
    "dependencies": PropTypes.array.isRequired,
    "onSelectedDependenciesChanged": PropTypes.func,
    "legend": PropTypes.bool
};

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