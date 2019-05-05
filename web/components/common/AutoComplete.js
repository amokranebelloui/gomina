// @flow
import React, {Fragment} from "react";
import "./AutoComplete.css"

type Props<T> = {
    value?: ?T;
    suggestions: Array<T>,
    idGetter?: T => ?string,
    labelGetter?: T => ?string,
    onChange?: ?T => void
}

type State<T> = {
    activeSuggestion: number, // The active selection's index
    filteredSuggestions: Array<any>, // The suggestions that match the user's input
    showSuggestions: boolean, // Whether or not the suggestion list is shown
    userInput: string, // What the user has entered
    selectedItem: ?T
}

class Autocomplete<T> extends React.Component<Props<T>, State<T>> {
    constructor(props: Props<T>) {
        super(props);
        const userInput = this.label(this.props.value) || "";
        const matchingSuggestions = this.props.suggestions.filter(s =>
            this.label(s).toLowerCase() === userInput.toLowerCase()
        );
        const newlySelected:?T = matchingSuggestions.length === 1 ? matchingSuggestions[0] : null;
        this.state = {
            activeSuggestion: 0,
            filteredSuggestions: [],
            showSuggestions: false,
            userInput: userInput,
            selectedItem: newlySelected
        };
    }
    id(suggestion: ?T): string {
        //$FlowFixMe
        return (this.props.idGetter && suggestion ? this.props.idGetter(suggestion) : suggestion) || '';
    }
    label(suggestion: ?T): string {
        //$FlowFixMe
        return (this.props.labelGetter && suggestion ? this.props.labelGetter(suggestion) : suggestion) || '';
    }
    onChange(e: SyntheticKeyboardEvent<any>) {
        const userInput = e.currentTarget.value;

        const filteredSuggestions = this.props.suggestions.filter(s =>
            this.label(s).toLowerCase().indexOf(userInput.toLowerCase()) > -1
        );

        const matchingSuggestions = this.props.suggestions.filter(s =>
            this.label(s).toLowerCase() === userInput.toLowerCase()
        );
        const newlySelected:?T = matchingSuggestions.length === 1 ? matchingSuggestions[0] : null;

        if (newlySelected !== this.state.selectedItem) {
            this.props.onChange && this.props.onChange(newlySelected)
        }
        // Update the user input and filtered suggestions, reset the active suggestion and make sure suggestions are shown
        this.setState({
            activeSuggestion: 0,
            filteredSuggestions: filteredSuggestions,
            showSuggestions: true,
            userInput: e.currentTarget.value,
            // FIXME Equality => selectedItem
            selectedItem: newlySelected
        });
    };
    onClick(suggestion: T) {
        // Update the user input and reset the rest of the state
        this.setState({
            activeSuggestion: 0,
            filteredSuggestions: [],
            showSuggestions: false,
            userInput: this.label(suggestion),
            selectedItem: suggestion
        });
        this.props.onChange && this.props.onChange(suggestion)
    };
    onKeyDown(e: SyntheticKeyboardEvent<any>) {
        if (e.keyCode === 13) { // enter key, update the input and close the suggestions
            const selectedSuggestion = this.state.filteredSuggestions[this.state.activeSuggestion];
            this.setState({
                activeSuggestion: 0,
                showSuggestions: false,
                userInput: this.label(selectedSuggestion),
                selectedItem: selectedSuggestion
            });
            this.props.onChange && this.props.onChange(selectedSuggestion)
        }
        else if (e.keyCode === 27) { // up arrow, decrement the index
            this.setState({ showSuggestions: false });
        }
        else if (e.keyCode === 38) { // up arrow, decrement the index
            if (this.state.activeSuggestion === 0) { return; }
            this.setState({ activeSuggestion: this.state.activeSuggestion - 1 });
        }
        else if (e.keyCode === 40) { // down arrow, increment the index
            if (this.state.activeSuggestion - 1 === this.state.filteredSuggestions.length) { return; }
            this.setState({ activeSuggestion: this.state.activeSuggestion + 1 });
        }
    };

    render() {
        let suggestionsListComponent;
        if (this.state.showSuggestions && this.state.userInput) {
            if (this.state.filteredSuggestions.length) {
                suggestionsListComponent = (
                    <ul class="suggestions">
                        {this.state.filteredSuggestions.map((suggestion, index) => {
                            return (
                                <li key={this.id(suggestion)}
                                    className={index === this.state.activeSuggestion ? "suggestion-active" : null}
                                    onClick={() => this.onClick(suggestion)}>
                                    {this.label(suggestion)}
                                </li>
                            );
                        })}
                    </ul>
                );
            }
            else {
                suggestionsListComponent = (
                    <div class="no-suggestions">
                        <em>No suggestions, you're on your own!</em>
                    </div>
                );
            }
        }
        // <span>{JSON.stringify(this.state.selectedItem)}</span>
        return (
            <Fragment>
                <div style={{display: 'inline-block', position: 'relative'}}>
                    <input type="text"
                           className={this.state.userInput && !this.state.selectedItem ? "suggestion-unknown-item" : null}
                           onChange={e => this.onChange(e)}
                           onKeyDown={e => this.onKeyDown(e)}
                           value={this.state.userInput} />
                    <div style={{position: 'absolute', zIndex: 5}}>
                        {suggestionsListComponent}
                    </div>
                </div>
            </Fragment>
        );
    }
}

export {Autocomplete}