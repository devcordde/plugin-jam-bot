<script setup lang="ts">
const { user } = useAuth()
const config = useRuntimeConfig()
const router = useRouter()

const errorType = ref<'no_team' | 'no_server' | 'provisioning' | 'success' | 'loading'>('loading')
const isProvisioning = ref(false)
const checkInterval = ref<any>(null)

const checkStatus = async () => {
  try {
    const headers = useRequestHeaders(['cookie'])
    const team = await $fetch('/api/teams/my-team', {
      baseURL: config.public.apiBase,
      credentials: 'include',
      headers
    }).catch(() => null)

    if (!team) {
      errorType.value = 'no_team'
      return
    }

    const serverInfo = await $fetch<{ exists: boolean, provisioning: boolean }>('/api/server/exists', {
      baseURL: config.public.apiBase,
      credentials: 'include',
      headers
    })

    if (serverInfo.exists) {
      errorType.value = 'success'
      isProvisioning.value = false
      stopPolling()
      return
    }

    if (serverInfo.provisioning) {
      errorType.value = 'provisioning'
      isProvisioning.value = true
      startPolling()
    } else {
      errorType.value = 'no_server'
      isProvisioning.value = false
      stopPolling()
    }
  } catch (e) {
    console.error('Failed to check status', e)
  }
}

const provisionServer = async () => {
  if (isProvisioning.value) return
  
  try {
    await $fetch('/api/server/provision', {
      method: 'POST',
      baseURL: config.public.apiBase,
      credentials: 'include'
    })
    isProvisioning.value = true
    errorType.value = 'provisioning'
    startPolling()
  } catch (e) {
    console.error('Failed to provision server', e)
  }
}

const startPolling = () => {
  if (checkInterval.value) return
  checkInterval.value = setInterval(checkStatus, 5000)
}

const stopPolling = () => {
  if (checkInterval.value) {
    clearInterval(checkInterval.value)
    checkInterval.value = null
  }
}

onMounted(() => {
  checkStatus()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-[60vh] text-center px-4">
    <div v-if="errorType === 'loading'" class="flex flex-col items-center">
      <UIcon name="i-heroicons-arrow-path" class="w-12 h-12 animate-spin text-primary mb-4" />
      <h1 class="text-2xl font-bold">Checking status...</h1>
    </div>

    <div v-else-if="errorType === 'no_team'" class="max-w-md">
      <UIcon name="i-heroicons-user-group" class="w-16 h-16 text-error mb-4 mx-auto" />
      <h1 class="text-3xl font-bold mb-2">Team Required</h1>
      <p class="text-gray-400 mb-6">
        You need to be part of a team to access the console. Please register for a team on our Discord server first.
      </p>
      <UButton to="https://discord.gg/your-invite" target="_blank" color="primary" icon="i-simple-icons-discord">
        Join Discord
      </UButton>
    </div>

    <div v-else-if="errorType === 'no_server' || errorType === 'provisioning' || errorType === 'success'" class="max-w-md">
      <UIcon 
        :name="errorType === 'provisioning' ? 'i-heroicons-cpu-chip' : (errorType === 'success' ? 'i-heroicons-check-circle' : 'i-heroicons-server-stack')" 
        class="w-16 h-16 mb-4 mx-auto"
        :class="{
          'text-primary animate-pulse': errorType === 'provisioning',
          'text-success': errorType === 'success',
          'text-warning': errorType === 'no_server'
        }"
      />
      <h1 class="text-3xl font-bold mb-2">
        {{ 
          errorType === 'provisioning' ? 'Provisioning Server...' : 
          (errorType === 'success' ? 'Server Ready!' : 'No Server Found') 
        }}
      </h1>
      <p class="text-gray-400 mb-6">
        {{ 
          errorType === 'provisioning' 
            ? 'Your team\'s server is currently being prepared. This usually takes a few minutes. Please wait.' 
            : (errorType === 'success' 
              ? 'Your server has been successfully provisioned. You can now access the console.' 
              : 'Your team has been registered, but no server has been provisioned yet.') 
        }}
      </p>
      
      <div class="flex flex-col gap-3 items-center">
        <UButton 
          v-if="errorType === 'no_server'"
          @click="provisionServer"
          color="primary"
          icon="i-heroicons-rocket-launch"
          size="lg"
        >
          Setup Server
        </UButton>
        
        <UButton 
          v-if="errorType === 'provisioning'"
          disabled
          color="neutral"
          variant="subtle"
          icon="i-heroicons-lock-closed"
          size="lg"
        >
          Provisioning in progress...
        </UButton>

        <UButton 
          v-if="errorType === 'success'"
          to="/app/console"
          color="success"
          icon="i-heroicons-arrow-right"
          size="lg"
        >
          Go to the console page
        </UButton>

        <p v-if="errorType === 'provisioning'" class="text-sm text-gray-500 italic">
          Locked for all team members while provisioning.
        </p>
      </div>
    </div>
  </div>
</template>
