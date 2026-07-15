<script setup lang="ts">
import SettingsGroup from "~/components/SettingsGroup.vue";

definePageMeta({
  layout: 'dashboard',
  title: 'Settings'
})

const config = useRuntimeConfig()
const isReinstalling = ref(false)
const toast = useToast()

const reinstallServer = async () => {
  if (!confirm('Are you sure you want to reinstall the server? This will delete the current container and provision a new one.')) {
    return
  }

  isReinstalling.value = true
  try {
    await $fetch(`${config.public.apiBase}/api/server/reinstall`, {
      method: 'POST',
      credentials: 'include'
    })
    toast.add({
      title: 'Success',
      description: 'Server reinstall initiated.',
      color: 'success'
    })
  } catch (e) {
    console.error('Failed to reinstall server', e)
    toast.add({
      title: 'Error',
      description: 'Failed to initiate server reinstall.',
      color: 'error'
    })
  } finally {
    isReinstalling.value = false
  }
}
</script>

<template>
  <div class="space-y-6">
    <SettingsGroup title="Danger Zone" description="These actions can result in data loss or downtime. Use with caution." color="error">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 rounded bg-gray-900/30 p-4">
        <div>
          <span class="font-medium text-white">Reinstall Server</span>
          <p class="text-sm text-gray-400">
            Deletes the current server container and provisions a new one.
            Use this if your server is stuck or broken.
          </p>
        </div>
        <UButton
            color="error"
            variant="solid"
            :loading="isReinstalling"
            icon="i-heroicons-arrow-path"
            @click="reinstallServer"
        >
          Reinstall
        </UButton>
      </div>
    </SettingsGroup>
  </div>
</template>

<style scoped>

</style>