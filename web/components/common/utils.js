import React from 'react';

const uniq = a => [...new Set(a)];

function uniqCount(arr) {
    const reduce = arr.reduce((acc, val) => {
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

export {uniq, uniqCount, flatMap, groupBy}
