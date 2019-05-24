// @flow
import * as React from "react"
import {library} from '@fortawesome/fontawesome-svg-core'
import {faStar} from '@fortawesome/free-solid-svg-icons'
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome/index.es';
import "./StarRating.css"

library.add(faStar);

type Props = {
    value?: ?number,
    count?: ?number,
    editable?: boolean,
    onRatingChange?: ?(number => void)
}

class StarRating extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    changeRating(rating: number) {
        //const rating = this.props.value === 1 && selectedRating === 1 ? 0 : selectedRating;
        //console.info("star click", rating);
        this.props.onRatingChange && this.props.onRatingChange(rating)
    }
    render() {
        const value = this.props.value || 0;
        const count = this.props.count || 5;

        const stars = [];
        for (let i = 0; i < count; i++) {
            const prefix = this.props.editable ? "editable-" : "";
            const className = prefix + (i < value ? "star-rating-selected" : "star-rating");
            stars.push(
                <FontAwesomeIcon key={i} icon="star" className={className}
                                 onClick={() => this.changeRating(i + 1)} />
            )
        }
        return (
            <div style={{display: 'inline-block', whiteSpace: 'nowrap'}}>
                {stars}
            </div>
        );
    }
}

export { StarRating }