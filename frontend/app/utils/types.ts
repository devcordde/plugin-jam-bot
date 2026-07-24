export interface User {
    id: string
    name: string
    picture: string
}

export interface TeamMember {
    userId: number
}

export interface TeamMeta {
    teamId: number,
    teamName: string,
    leaderId: string,
    roleId: string,
    voiceChannelId: string,
    textChannelId: string,
    projectDescription: string,
    projectUrl: string
}

export interface Team {
    id: number,
    jamId: number,
    members: TeamMember[],
    meta: TeamMeta
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