<script setup lang="ts">
import type {Team} from "~/utils/types";

definePageMeta({
  layout: 'dashboard',
  title: 'Team Settings'
})

const config = useRuntimeConfig()
const toast = useToast()

const team = ref<Team | null>(null)
const loading = ref(true)
const saving = ref(false)

const form = ref({
  projectDescription: '',
  projectUrl: ''
})

const fetchTeam = async () => {
  loading.value = true
  try {
    const data = await $fetch<Team>(`/api/teams/my-team`, {
      baseURL: config.public.apiBase,
      credentials: 'include',
      headers: useRequestHeaders(['cookie'])
    })
    team.value = data
    form.value.projectDescription = data.meta.projectDescription || ''
    form.value.projectUrl = data.meta.projectUrl || ''
  } catch (error) {
    console.error('Failed to fetch team:', error)
    toast.add({
      title: 'Error',
      description: 'Failed to fetch team information',
      color: 'error'
    })
  } finally {
    loading.value = false
  }
}

const saveMeta = async () => {
  saving.value = true
  try {
    await $fetch(`/api/teams/my-team/meta`, {
      method: 'PUT',
      baseURL: config.public.apiBase,
      body: form.value,
      credentials: 'include',
      headers: useRequestHeaders(['cookie'])
    })
    toast.add({
      title: 'Success',
      description: 'Team information updated',
      color: 'success'
    })
    await fetchTeam()
  } catch (error) {
    console.error('Failed to update team:', error)
    toast.add({
      title: 'Error',
      description: 'Failed to update team information',
      color: 'error'
    })
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchTeam()
})
</script>

<template>
  <div class="p-4 space-y-6">
    <UCard v-if="team">
      <template #header>
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text">Team Information: {{ team.meta.teamName }}</h3>
          <UBadge color="primary" variant="subtle">ID: {{ team.id }}</UBadge>
        </div>
      </template>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <p class="text-sm ">Leader ID</p>
          <UBadge color="neutral" variant="subtle" class="font-mono">{{ team.meta.leaderId }}</UBadge >
        </div>
        <div>
          <p class="text-sm text-gray-500">Role ID</p>
          <UBadge color="neutral" variant="subtle" class="font-mono">{{ team.meta.roleId }}</UBadge>
        </div>
        <div>
          <p class="text-sm text-gray-500">Text Channel ID</p>
          <UBadge color="neutral" variant="subtle" class="font-mono">{{ team.meta.textChannelId }}</UBadge>
        </div>
        <div>
          <p class="text-sm text-gray-500">Voice Channel ID</p>
          <UBadge color="neutral" variant="subtle" class="font-mono">{{ team.meta.voiceChannelId }}</UBadge>
        </div>
      </div>
    </UCard>

    <UCard v-if="team">
      <template #header>
        <h3 class="text-lg font-semibold">Project Details</h3>
      </template>

      <form @submit.prevent="saveMeta" class="space-y-4">
        <UFormField label="Project URL" help="Link to your project (e.g. GitHub, GitLab)">
          <UInput v-model="form.projectUrl" placeholder="https://github.com/..." class="w-full" />
        </UFormField>

        <UFormField label="Project Description" help="Tell us about your project">
          <UTextarea v-model="form.projectDescription" placeholder="Our project is about..." class="w-full" :rows="5" />
        </UFormField>

        <div class="flex justify-end">
          <UButton type="submit" color="primary" :loading="saving">
            Save Changes
          </UButton>
        </div>
      </form>
    </UCard>

    <div v-else-if="loading" class="flex justify-center p-12">
      <UIcon name="i-heroicons-arrow-path" class="animate-spin h-8 w-8 text-gray-400" />
    </div>

    <UCard v-else>
      <div class="text-center p-8 text-gray-500">
        No team found. Are you part of a team?
      </div>
    </UCard>
  </div>
</template>

<style scoped>

</style>