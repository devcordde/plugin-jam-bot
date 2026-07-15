export interface User {
    id: string
    name: string
    picture: string
}

export interface TeamMember {
    userId: number
}

export interface Team {
    id: number,
    jamId: number,
    members: TeamMember[],
    meta: {
        teamName: string,
        leaderId: string,
        roleId: number,
        voiceChannelId: number,
        textChannelId: number,
    }
}