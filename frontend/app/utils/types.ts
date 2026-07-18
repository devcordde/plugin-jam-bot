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

export interface FileInfo {
    name: string
    path: string
    isDirectory: boolean
    readOnly: boolean
    size: number
    lastModified: string
}

export type ServerStatus = 'RUNNING' | 'STOPPED' | 'STARTING_STOPPING' | 'VOID'