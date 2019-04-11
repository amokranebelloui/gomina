// @flow
import React, {Fragment} from "react";
import "./Autocomplete.css"

type T = any

type Props = {
    suggestions: Array<T>,
    idProperty?: string,
    labelProperty?: string
}

type State = {
    activeSuggestion: number, // The active selection's index
    filteredSuggestions: Array<any>, // The suggestions that match the user's input
    showSuggestions: boolean, // Whether or not the suggestion list is shown
    userInput: string, // What the user has entered
    selectedItem: T
}

class Autocomplete extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            activeSuggestion: 0,
            filteredSuggestions: [],
            showSuggestions: false,
            userInput: "",
            selectedItem: null
        };
    }
    id(suggestion: T) {
        return this.props.idProperty ? suggestion[this.props.idProperty] : suggestion;
    }
    label(suggestion: T) {
        return this.props.labelProperty ? suggestion[this.props.labelProperty] : suggestion;
    }
    onChange(e: SyntheticKeyboardEvent<any>) {
        const userInput = e.currentTarget.value;

        const filteredSuggestions = this.props.suggestions.filter(s =>
            this.label(s).toLowerCase().indexOf(userInput.toLowerCase()) > -1
        );

        const matchingSuggestions = this.props.suggestions.filter(s =>
            this.label(s).toLowerCase() === userInput.toLowerCase()
        );

        // Update the user input and filtered suggestions, reset the active suggestion and make sure suggestions are shown
        this.setState({
            activeSuggestion: 0,
            filteredSuggestions: filteredSuggestions,
            showSuggestions: true,
            userInput: e.currentTarget.value,
            // FIXME Equality => selectedItem
            selectedItem: matchingSuggestions.length == 1 ? matchingSuggestions[0] : null
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
    };
    onKeyDown(e: SyntheticKeyboardEvent<any>) {
        //const { activeSuggestion, filteredSuggestions } = this.state;
        if (e.keyCode === 13) { // enter key, update the input and close the suggestions
            const selectedSuggestion = this.state.filteredSuggestions[this.state.activeSuggestion];
            this.setState({
                activeSuggestion: 0,
                showSuggestions: false,
                userInput: this.label(selectedSuggestion),
                selectedItem: selectedSuggestion
            });
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
                            let className;

                            // Flag the active suggestion with a class
                            if (index === this.state.activeSuggestion) {
                                className = "suggestion-active";
                            }

                            return (
                                <li key={this.id(suggestion)} className={className} onClick={() => this.onClick(suggestion)}>
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
        return (
            <Fragment>
                <input type="text" onChange={e => this.onChange(e)} onKeyDown={e => this.onKeyDown(e)} value={this.state.userInput}/>
                <span>{JSON.stringify(this.state.selectedItem)}</span>
                {suggestionsListComponent}
            </Fragment>
        );
    }
}

export {Autocomplete}