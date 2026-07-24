<script setup lang="ts">
import SettingsGroup from "~/components/SettingsGroup.vue";

definePageMeta({
  layout: 'dashboard',
  title: 'Settings'
})

const config = useRuntimeConfig()
const isReinstalling = ref(false)
const toast = useToast()
const modalOpen = ref(false)

const reinstallServer = async () => {
  modalOpen.value = false

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
    <SettingsGroup title="Danger Zone" color="error">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 rounded py-4">
        <div>
          <span class="font-medium">Reinstall Server</span>
          <p class="text-sm ">
            Deletes the current server container and provisions a new one.
            Use this if your server is stuck or broken.
          </p>
        </div>
        <UModal
            v-model:open="modalOpen"
        title="Reinstall Server"
        description="Are you sure you want to reinstall the server? This will delete the current container and provision a new one.">
          <UButton
              color="error"
              variant="solid"
              :loading="isReinstalling"
              icon="i-heroicons-arrow-path"
          >
            Reinstall
          </UButton>
          <template #body>
            <div class="flex justify-end gap-2">
              <UButton color="neutral" variant="soft" @click="modalOpen = false">
                Cancel
              </UButton>
              <UButton color="error" @click="reinstallServer">
                Reinstall
              </UButton>
            </div>
          </template>
        </UModal>
      </div>
    </SettingsGroup>
  </div>
</template>