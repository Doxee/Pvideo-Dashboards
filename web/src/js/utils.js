
// UTILITY ALPHABETICAL SORTING FUNCTION
const getObjectKeysAlphabetical = (obj) => {
    var keys = [],
        key;

    for (key in obj) {
        if (obj.hasOwnProperty(key))
            keys.push(key);
    }

    keys.sort();
    return keys;
}

export {getObjectKeysAlphabetical};

