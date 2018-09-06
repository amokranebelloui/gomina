import React from 'react';

function groupBy(list, property) {
    return list.reduce((result, obj) => {
        const value = obj[property];
        (result[value] = result[value] || []).push(obj);
        return result;
    }, {});
};

export {groupBy}
