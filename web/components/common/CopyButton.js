import React from "react";

class CopyButton extends React.Component {
    copyToClipboard(text) {
        //event.originalTarget.parentNode.children[2].select()
        this.textEdit.select();
        let successful = document.execCommand('copy');
        console.log(successful, text, this.textEdit.value);
    }
    render() {
        return (
            <span style={{
                display: 'inline-block',
                backgroundColor: 'black', color: 'white',
                padding: 1, fontSize: 9, borderRadius: 4,
                cursor: 'pointer'}}
                  onClick={e => this.copyToClipboard(this.props.value)}>

                <span style={{padding: 1, verticalAlign: 'top', align: 'middle'}}>
                    &gt;<input type="text" readOnly
                               ref={node => this.textEdit = node}
                               value={this.props.value || ''}
                               style={{opacity: 0, backgroundColor: 'red', width: 1, height: 1, border: 0}} />
                    &gt;
                </span>
            </span>
        )
    }
}

export { CopyButton }
