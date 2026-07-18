<script setup lang="ts">
import FileEditor from "~/components/FileEditor.vue";
import type {FileInfo} from "~/utils/types";

definePageMeta({
  layout: 'dashboard',
  title: 'Edit File'
})

const route = useRoute()
const config = useRuntimeConfig()
const path = computed(() => route.query.path as string)
const filename = computed(() => path.value?.split('/').pop() || 'unknown')

const editorValue = ref('')
const loading = ref(true)
const saving = ref(false)
const fileInfo = ref<FileInfo | null>(null)

const fetchFileInfo = async () => {
  if (!path.value) return
  try {
    const parentPath = path.value.includes('/') ? path.value.substring(0, path.value.lastIndexOf('/')) : ''
    const files = await $fetch<FileInfo[]>(`/api/server/files/list`, {
      baseURL: config.public.apiBase,
      query: {path: parentPath},
      credentials: 'include',
      headers: useRequestHeaders(['cookie'])
    })
    fileInfo.value = files.find(f => f.path === path.value) || null
  } catch (error) {
    console.error('Failed to fetch file info:', error)
  }
}

const fetchContent = async () => {
  if (!path.value) return
  loading.value = true
  try {
    const data = await $fetch(`/api/server/files/content`, {
      baseURL: config.public.apiBase,
      query: {path: path.value},
      credentials: 'include',
      headers: useRequestHeaders(['cookie']),
      parseResponse: (txt) => txt
    })
    editorValue.value = data as string
  } catch (error) {
    console.error('Failed to fetch file content:', error)
  } finally {
    loading.value = false
  }
}

const saveFile = async () => {
  if (!path.value) return
  saving.value = true
  try {
    await $fetch(`/api/server/files/content`, {
      method: 'POST',
      baseURL: config.public.apiBase,
      params: {path: path.value},
      body: editorValue.value,
      credentials: 'include',
      headers: {
        ...useRequestHeaders(['cookie']),
        'Content-Type': 'text/plain'
      }
    })
    useToast().add({
      title: 'Success',
      description: 'File saved successfully',
      color: 'success'
    })
  } catch (error) {
    console.error('Failed to save file:', error)
    useToast().add({
      title: 'Error',
      description: 'Failed to save file',
      color: 'error'
    })
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await fetchContent()
  await fetchFileInfo()
})
</script>

<template>
  <div class="flex flex-col gap-4 h-full">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <UButton
            icon="i-lucide-arrow-left"
            variant="ghost"
            @click="navigateTo('/app/files')"
        />
        <h1 class="text-xl font-semibold">{{ filename }}</h1>
        <span class="text-gray-500 text-sm font-mono">{{ path }}</span>
      </div>
      <div class="flex gap-2">
        <UButton
            v-if="fileInfo && !fileInfo.readOnly"
            label="Save"
            icon="i-lucide-save"
            :loading="saving"
            @click="saveFile"
        />
      </div>
    </div>

    <UCard class="flex-1 overflow-hidden" :ui="{ body: 'p-0 h-full' }">
      <div v-if="loading" class="flex items-center justify-center h-full p-8">
        <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin"/>
      </div>
      <FileEditor v-else-if="fileInfo" v-model="editorValue" :filename="filename" :read-only="fileInfo.readOnly"
                  class="h-full"/>
    </UCard>
  </div>
</template>

<style scoped>
:deep(.cm-wrapper) {
  height: 100%;
}
</style>