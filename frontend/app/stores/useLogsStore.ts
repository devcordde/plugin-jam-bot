import { useWebSocket } from '@vueuse/core'

export const useLogsStore = defineStore('logs', () => {
    interface LogEntry {
        timestamp?: string
        level?: string
        message: string
    }

    const config = useRuntimeConfig()

    const logs = ref<LogEntry[]>([])

    let lastLevel = ''

    function parseLog(rawLog: string): LogEntry {
        // Regex to match [HH:mm:ss LEVEL]: message
        const match = rawLog.match(/^\[(\d{2}:\d{2}:\d{2})\s+([A-Z]+)\]:\s*(.*)$/)
        if (match) {
            lastLevel = match[2] || lastLevel
            return {
                timestamp: match[1],
                level: match[2],
                message: match[3] || ''
            }
        }
        return {
            level: lastLevel,
            message: rawLog
        }
    }

    const protocol = config.public.apiBase.startsWith('https') ? 'wss' : 'ws'
    const host = config.public.apiBase.replace(/^https?:\/\//, '')
    const wsUrl = `${protocol}://${host}/api/server/logs/ws`

    const { status, data, open: openWs } = useWebSocket(wsUrl, {
        autoConnect: false,
        onConnected: () => {
            logs.value = []
            console.log('WebSocket connection established')
        },
        onDisconnected: (ws) => {
            console.log('WebSocket connection closed')
        }
    })

    const isConnected = computed(() => status.value === 'OPEN')

    watch(data, (newLog) => {
        if (!newLog) return;

        if (newLog) {

            const lines = newLog.split('\n');
            const newEntries: LogEntry[] = [];

            for (const line of lines) {
                if (line.trim() === '' && lines.length > 1) continue;
                newEntries.push(parseLog(line));
            }

            logs.value.push(...newEntries);

            if (logs.value.length > 1000) {
                logs.value = logs.value.slice(-1000);
            }
        }
    })

    return {
        logs,
        isConnected,
        openWs
    }
})