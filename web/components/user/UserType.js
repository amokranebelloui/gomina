type UserType = {
    id: string,
    login: string,
    shortName: string,
    firstName: ?string,
    lastName: ?string,
    accounts: Array<string>,
    disabled: boolean
}

type UserDataType = {
    login: string,
    shortName: string,
    firstName: ?string,
    lastName: ?string,
    accounts: Array<string>,
}

export {UserType, UserDataType}