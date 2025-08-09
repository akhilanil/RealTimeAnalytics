export interface UserDetailsResponse {
    userDetails: Array<UserDetails>
}

export interface UserDetails {

    userId: string
    sessionCount: number

}