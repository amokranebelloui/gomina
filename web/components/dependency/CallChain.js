import React from 'react'
import PropTypes from 'prop-types'

class CallChain extends React.Component {
    constructor(props) {
        super(props);
        //console.info("chain constructor", this.props.chain);
        const open = {};
        if (this.props.chain) {
            open[this.props.chain.projectId] = true;
        }
        this.state = {open: open};
        //this.state = {open: this.props.chain}
    }
    componentWillReceiveProps(nextProps) {
        console.info("chain willReceiveProps", nextProps);
        if (nextProps.chain != this.props.chain) {
            const chain = nextProps.chain;
            if (chain) {
                const open = {};
                open[chain.projectId] = true;
                this.setState({open: open})
            }
        }
    }
    openAll(chain, path) {
        const openPaths = {};
        openPaths[path] = true;
        (chain.calls || []).forEach(call => {
            Object.assign(openPaths, this.openAll(call, path + "/" + call.projectId))
        });
        return openPaths
    }
    onOpenAll() {
        if (this.props.chain) {
            const openPaths = this.openAll(this.props.chain, this.props.chain.projectId);
            this.setState({open: openPaths})
        }
    }
    onCloseAll() {
        if (this.props.chain) {
            const open = {"chain.projectId": true};
            this.setState({open: open})
        }
    }
    onPathOpened1(path) {
        //console.info("OPENED", path);
        const newOpen = Object.assign({}, this.state.open);
        newOpen[path] = true;
        this.setState({open: newOpen})
    }
    onPathClosed1(path) {
        //console.info("CLOSED", path);
        const newOpen = Object.assign({}, this.state.open);
        newOpen[path] = false;
        this.setState({open: newOpen})
    }
    dependencySelected(child, parent) {
        this.props.onDependencySelected && this.props.onDependencySelected(child, parent)
    }
    render() {
        return (
            <div>
                <button onClick={e => this.onOpenAll()}>OPEN ALL</button>
                <button onClick={e => this.onCloseAll()}>CLOSE ALL</button>
            <Call chain={this.props.chain}
                  padding={0} displayNode={this.props.displayFirst}
                  openPaths={this.state.open}
                  path={(this.props.chain||{}).projectId}
                  onPathOpened={e => this.onPathOpened1(e)}
                  onPathClosed={e => this.onPathClosed1(e)}
                  onDependencySelected={(child, parent) => this.dependencySelected(child, parent)}
            />
            </div>
        )
    }
}

CallChain.propTypes = {
    chain: PropTypes.object,
    displayFirst: PropTypes.bool,
    onDependencySelected: PropTypes.func
};


/// projectId: String, val recursive: Boolean, val functions: List<FunctionDetail> = emptyList(), val calls

class Call extends React.Component {
    constructor(props) {
        super(props)
    }
    isOpen(path) {
        //console.info('is open', path, this.props.openPaths[path], this.props.openPaths[path] == true);
        return this.props.openPaths[path] == true
    }
    onPathOpened(path) {
        //console.info("opened", path);
        this.props.onPathOpened && this.props.onPathOpened(path)
    }
    onPathClosed(path) {
        //console.info("closed", path);
        this.props.onPathClosed && this.props.onPathClosed(path)
    }
    dependencySelected(child, parent) {
        this.props.onDependencySelected && this.props.onDependencySelected(child, parent)
    }
    render() {
        const chain = this.props.chain || {};
        const displayNode = this.props.displayNode || false;
        const padding = this.props.padding || 0;
        const nextPadding = displayNode ? padding + 14 : 0;
        //const functions = chain.functions;
        //functions.map(u => u.name)
        const usageCount = (chain.functions||[]).length;
        const callCount = (chain.calls||[]).length;
        return (
            <div>
                {displayNode &&
                    <span style={{paddingLeft: padding}}>
                        {callCount == 0
                            ? <span style={{display: 'inline-block', width: '12px', color: '#f5f5f5'}}><b>&rarr;&nbsp;</b></span>
                            : this.isOpen(this.props.path)
                            ? <span style={{display: 'inline-block', width: '12px'}} onClick={e => this.onPathClosed(this.props.path)}><b>&darr;&nbsp;</b></span>
                            : <span style={{display: 'inline-block', width: '12px'}} onClick={e => this.onPathOpened(this.props.path)}><b>&rarr;&nbsp;</b></span>
                        }
                        <span onDoubleClick={e => this.dependencySelected(this.props.chain, this.props.parent)}>
                            {chain.projectId} {chain.recursive && '@'}
                            </span>
                        <span style={{color: 'lightgray'}}>
                            {usageCount > 0 &&
                                "(" + usageCount + " usages)"
                            }
                        </span>
                        {/*<span style={{color: 'red'}}>{callCount>0 && "(" + callCount + " calls)"}</span>*/}
                    </span>
                }

                {(chain.calls||[]).map( call =>
                    this.isOpen(this.props.path) &&
                            <Call key={call.projectId}
                                  chain={call}
                                  parent={chain}
                                  padding={nextPadding} displayNode={true}
                                  openPaths={this.props.openPaths}
                                  path={this.props.path + "/" + call.projectId}
                                  onPathOpened={e => this.onPathOpened(e)}
                                  onPathClosed={e => this.onPathClosed(e)}
                                  onDependencySelected={(from, to) => this.dependencySelected(from, to)}
                            />

                )}
            </div>
        )
    }
}

Call.propTypes = {
    chain: PropTypes.object,
    parent: PropTypes.object,
    padding: PropTypes.number.required,
    displayNode: PropTypes.bool,
    path: PropTypes.string,
    openPaths: PropTypes.object,
    onPathOpened: PropTypes.func,
    onPathClosed: PropTypes.func,
    onDependencySelected: PropTypes.func
};

export { CallChain }