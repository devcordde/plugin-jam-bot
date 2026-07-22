<script setup lang="ts">
import type {ServerStatus} from "~/utils/types";

definePageMeta({
  layout: 'dashboard',
  title: 'Console'
})

const config = useRuntimeConfig()

const command = ref('')
const terminal = ref<HTMLElement | null>(null)
const serverStatus = ref<ServerStatus | null>(null)

const logsStore = useLogsStore()
const { logs, isConnected } = storeToRefs(logsStore)
const { openWs } = logsStore

onMounted(async () => {
  openWs()
  await fetchServerStatus()
})

watch(logs, async () => {
  await nextTick()
  if (terminal.value) {
    terminal.value.scrollTop = terminal.value.scrollHeight
  }
}, { deep: true })

const statusColors = computed(() => {
  switch (serverStatus.value) {
    case 'RUNNING':
      return { light: 'bg-green-400', dark: 'bg-green-500' }
    case 'STARTING_STOPPING':
      return { light: 'bg-yellow-400', dark: 'bg-yellow-500' }
    case 'STOPPED':
      return { light: 'bg-red-400', dark: 'bg-red-500' }
    default:
      return { light: 'bg-gray-400', dark: 'bg-gray-500' }
  }
})

const fetchServerStatus = async () => {
  try {
    serverStatus.value = await $fetch(`/api/server/status`, {
      baseURL: config.public.apiBase,
      credentials: 'include',
      headers: useRequestHeaders(['cookie'])
    })
  } catch (error) {
    console.error('Failed to fetch server status:', error)
  }
  setTimeout(fetchServerStatus, 1000)
}

async function sendPowerAction(action: 'START' | 'STOP' | 'RESTART') {
  await $fetch(`${config.public.apiBase}/api/server/power`, {
    method: 'POST',
    body: {signal: action},
    credentials: 'include'
  })
  openWs()
}

async function sendCommand() {
  if (!command.value.trim()) return
  const cmd = command.value
  command.value = ''
  await $fetch(`${config.public.apiBase}/api/server/command`, {
    method: 'POST',
    body: {command: cmd},
    credentials: 'include'
  })
}
</script>

<template>
  <div class="grid gap-5 w-full">
    <!-- status bar -->
    <div class="flex gap-5 w-full">
      <div class="flex gap-5 bg-muted p-5 rounded items-center">
        <span class="font-bold">Status </span>
        <template v-if="serverStatus === 'RUNNING'">
          <PulsingCircle :color-light-class="statusColors.light" :color-dark-class="statusColors.dark"/>
        </template>
        <template v-else-if="serverStatus === 'STARTING_STOPPING'">
          <PulsingCircle :color-light-class="statusColors.light" :color-dark-class="statusColors.dark"/>
        </template>
        <template v-else-if="serverStatus === 'STOPPED'">
          <Icon name="lucide:server" :class="statusColors.light" class="w-6 h-6"/>
        </template>
        <template v-else>
          <Icon name="lucide:skull" :class="statusColors.light" class="w-6 h-6"/>
        </template>
      </div>
      <!-- power actions -->
      <div class="flex gap-5 bg-muted p-5 rounded w-full">
        <UButton icon="i-heroicons-play" color="success" variant="subtle" @click="sendPowerAction('START')">Start
        </UButton>
        <UButton icon="i-heroicons-stop" color="error" variant="subtle" @click="sendPowerAction('STOP')">Stop</UButton>
        <UButton icon="i-heroicons-arrow-path" color="secondary" variant="subtle" @click="sendPowerAction('RESTART')">
          Restart
        </UButton>
      </div>
    </div>
    <!-- console window -->
    <div class="flex flex-col gap-2">
      <div ref="terminal"
           class="bg-slate-950 border-2 border-gray-800 p-5 rounded h-200 overflow-y-auto font-mono text-sm text-gray-200 whitespace-pre-wrap">
        <div v-for="(log, index) in logs" :key="index"
             class="flex gap-2 py-0.5 items-start">
          <div v-if="log.timestamp" class="flex shrink-0 overflow-hidden rounded bg-gray-800 text-xs">
            <span class="px-1.5 py-0.5 text-gray-400 tabular-nums">{{ log.timestamp }}</span>
            <span
                class="px-1.5 py-0.5 font-bold"
                :class="{
              'bg-blue-500/20 text-blue-400': log.level === 'INFO',
              'bg-yellow-500/20 text-yellow-400': log.level === 'WARN',
              'bg-red-500/20 text-red-400': log.level === 'ERROR',
              'bg-gray-700/50 text-gray-400': !['INFO', 'WARN', 'ERROR'].includes(log.level || '')
            }"
            >
            {{ log.level }}
          </span>
          </div>
          <span
              class="break-words"
              :class="{
            'text-yellow-400/90': log.level === 'WARN',
            'text-red-400/90': log.level === 'ERROR',
            'text-gray-200': log.level !== 'WARN' && log.level !== 'ERROR'
          }"
          >{{ log.message }}</span>
        </div>
      </div>
      <UFieldGroup >
        <UInput :disabled="serverStatus != 'RUNNING'" v-model="command" placeholder="/" class="flex-grow" @keyup.enter="sendCommand"/>
        <UButton :disabled="serverStatus != 'RUNNING'" icon="lucide:send-horizontal" @click="sendCommand"></UButton>
      </UFieldGroup>
    </div>
  </div>
</template>