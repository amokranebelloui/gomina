import React from 'react';

const uniq = a => [...new Set(a)];

function uniqCount(arr) {
    const reduce = (arr||[]).reduce((acc, val) => {
        acc[val] = (acc[val] || 0) + 1;
        //console.log(acc);
        return acc;
    }, {});
    return Object.keys(reduce).map(k => { return {'value': k, 'count': reduce[k]}});
}

const flatMap = (arr, f) => arr.map(f).reduce((x,y) => x.concat(y), []);

function groupBy(list, property) {
    return list.reduce((result, obj) => {
        const value = obj[property];
        (result[value] = result[value] || []).push(obj);
        return result;
    }, {});
}

function matchesList(itemValues, selectedValues) {
    if (selectedValues) {
        if (selectedValues.length > 1 || selectedValues.length === 1 && selectedValues[0]) {
            const values = (itemValues||[]);
            return (selectedValues||[]).find(value => values.indexOf(value) !== -1);
        }
        else if (selectedValues.length === 1 && !selectedValues[0]) {
            return itemValues && itemValues.length === 0
        }
    }
    return true
}

function splitTags(tags: ?string) {
    try {
        return tags != null ? tags.split(',') : [];
    }
    catch (e) {
        console.error("Error splitting", tags, typeof tags);
        return []
    }
}

function joinTags(tags: ?Array<string>) {
    return tags != null && tags.length > 0 ? tags.join(',') : null;
}

export {uniq, uniqCount, flatMap, groupBy, matchesList, splitTags, joinTags}
