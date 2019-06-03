import type {EventType} from "./Events";

function eventMatchesSearch(event: EventType|string, search: ?string) {
    return event.group && event.group.match(new RegExp(search || "", "i")) ||
           event.type && event.type.match(new RegExp(search || "", "i")) ||
           event.message && event.message.match(new RegExp(search || "", "i"));
}

function filterEvents(events: ?Array<EventType>, search: ?string): ?Array<EventType> {
    return events && events.filter(l => eventMatchesSearch(l, search)) || []
}

export {filterEvents}
