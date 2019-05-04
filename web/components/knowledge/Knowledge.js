// @flow
import React, {Fragment} from "react"
import type {ComponentRefType} from "../component/ComponentType";
import type {UserRefType} from "../misc/UserType";
import {StarRating} from "../common/StarRating";


type KnowledgeType = {
    component: ComponentRefType,
    user: UserRefType,
    knowledge?: ?number
}

type Props = {
    knowledge?: ?Array<KnowledgeType>,
    hideUser?: boolean,
    hideComponent?: boolean
}

class Knowledge extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }
    render() {
        const knowledge = (this.props.knowledge || []).sort((a: KnowledgeType, b: KnowledgeType) => {
            const aString = (100 - (a.knowledge || 0)) + (a.user.shortName || '') + (a.component.label || '');
            const bString = (100 - (b.knowledge || 0)) + (b.user.shortName || '') + (b.component.label || '');
            return aString > bString ? 1 : -1;
            }
        );
        return (
            <div>
                {knowledge.map(k =>
                    <Fragment>
                        <StarRating value={k.knowledge} />&nbsp;
                        {!this.props.hideUser && <span>{k.user.shortName}&nbsp;</span>}
                        {!this.props.hideComponent && <span>{k.component.label}&nbsp;</span>}
                        <br/>
                    </Fragment>
                )}
            </div>
        );
    }
}

function componentKnowledge(knowledgeList: ?Array<KnowledgeType>, componentId: string, userId: string) {
    const knowledge = knowledgeList && knowledgeList.find(k => k.component.id === componentId && k.user.id === userId);
    return knowledge && knowledge.knowledge
}

export { Knowledge, componentKnowledge }
export type {KnowledgeType}