<script setup lang="ts">
import type {TableColumn, TableRow} from "@nuxt/ui"
import type { FileInfo } from '~/utils/types'
import {UButton, UCheckbox, UDropdownMenu, UIcon} from "#components";

const toast = useToast()

definePageMeta({
  layout: 'dashboard',
  title: 'Files'
})

const config = useRuntimeConfig()
const currentPath = ref('')
const files = ref<FileInfo[]>([])
const loading = ref(true)

const sorting = ref([{
  id: 'name',
  desc: false
}])

const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

const fetchFiles = async () => {
  loading.value = true
  try {
    files.value = await $fetch(`/api/server/files/list`, {
      baseURL: config.public.apiBase,
      query: { path: currentPath.value },
      credentials: 'include',

      headers: useRequestHeaders(['cookie'])
    })
    console.log('Files:', files.value)
  } catch (error) {
    console.error('Failed to fetch files:', error)
  } finally {
    loading.value = false
  }
}

const navigate = (file: FileInfo) => {
  if (file.isDirectory) {
    currentPath.value = file.path
    fetchFiles()
  } else {
    navigateTo({
      path: '/app/files/edit',
      query: { path: file.path }
    })
  }
}

const goBack = () => {
  if (!currentPath.value) return
  const parts = currentPath.value.split('/')
  parts.pop()
  currentPath.value = parts.join('/')
  fetchFiles()
}

onMounted(() => {
  fetchFiles()
})

const directoryFirstSort = (
    getValue: (file: FileInfo) => string | number
) => {
  return (rowA: TableRow<FileInfo>, rowB: TableRow<FileInfo>) => {
    const a = rowA.original
    const b = rowB.original

    if (a.isDirectory !== b.isDirectory) {
      return a.isDirectory ? -1 : 1
    }

    const valA = getValue(a)
    const valB = getValue(b)

    if (valA < valB) return -1
    if (valA > valB) return 1
    return 0
  }
}

const getBreadcrumbs = computed(() => {
  if (!currentPath.value) return [{ label: 'root', path: '' }]
  const parts = currentPath.value.split('/')
  const breadcrumbs = [{ label: 'root', path: '' }]
  let path = ''
  for (const part of parts) {
    path = path ? `${path}/${part}` : part
    breadcrumbs.push({ label: part, path })
  }
  return breadcrumbs
})

const navigateToBreadcrumb = (path: string) => {
  currentPath.value = path
  fetchFiles()
}

const isDragging = ref(false)
const dragCounter = ref(0)

const onDragEnter = (e: DragEvent) => {
  e.preventDefault()
  dragCounter.value++
  if (e.dataTransfer?.types.includes('Files')) {
    isDragging.value = true
  }
}

const onDragLeave = (e: DragEvent) => {
  e.preventDefault()
  dragCounter.value--
  if (dragCounter.value <= 0) {
    isDragging.value = false
    dragCounter.value = 0
  }
}

const onDrop = async (e: DragEvent) => {
  e.preventDefault()
  isDragging.value = false
  dragCounter.value = 0
  
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    await uploadFiles(Array.from(files))
  }
}

const deleteFile = async (path: string) => {
  await $fetch(`/api/server/files/delete`, {
    method: 'POST',
    baseURL: config.public.apiBase,
    query: { path },
    credentials: 'include',
    headers: {
      ...useRequestHeaders(['cookie'])
    }
  })
  toast.add({
    title: 'Success',
    description: `Deleted ${path}`,
    color: 'success'
  })

  await fetchFiles()
}

const uploadFiles = async (filesToUpload: File[]) => {
  for (const file of filesToUpload) {
    const formData = new FormData()
    formData.append('file', file)
    
    try {
      await $fetch(`/api/server/files/upload`, {
        method: 'POST',
        baseURL: config.public.apiBase,
        query: { path: currentPath.value },
        body: formData,
        credentials: 'include',
        headers: {
          ...useRequestHeaders(['cookie'])
        }
      })
      toast.add({
        title: 'Success',
        description: `Uploaded ${file.name}`,
        color: 'success'
      })
    } catch (error) {
      console.error('Failed to upload file:', error)
      toast.add({
        title: 'Error',
        description: `Failed to upload ${file.name}`,
        color: 'error'
      })
    }
  }
  await fetchFiles()
}

const getActions = (file: FileInfo) => {
  const actions = []
  if (!file.isDirectory) {
    actions.push({
      label: file.readOnly ? 'View' : 'Edit',
      icon: 'i-lucide-edit',
      onSelect: () => navigateTo({
        path: '/app/files/edit',
        query: { path: file.path }
      })
    })
  }
  actions.push({
    label: 'Delete',
    icon: 'i-lucide-trash-2',
    color: 'error' as const,
    disabled: file.readOnly,
    onSelect: () => deleteFile(file.path)
  })
  return [actions]
}

const columns: TableColumn<FileInfo>[] = [{
  id: 'select',
  header: ({ table }) => h(UCheckbox, {
    'modelValue': table.getIsSomePageRowsSelected() ? 'indeterminate' : table.getIsAllPageRowsSelected(),
    'onUpdate:modelValue': (value: boolean | 'indeterminate') => table.toggleAllPageRowsSelected(!!value),
    'aria-label': 'Select all'
  }),
  cell: ({ row }) => h(UCheckbox, {
    'modelValue': row.getIsSelected(),
    'onUpdate:modelValue': (value: boolean | 'indeterminate') => row.toggleSelected(!!value),
    'aria-label': 'Select row'
  }),
  enableSorting: false,
  enableHiding: false
}, {
  accessorKey: 'name',
  sortingFn: directoryFirstSort((f) => f.name.toLowerCase()),
  header: ({ column }) => {
    const isSorted = column.getIsSorted()

    return h(UButton, {
      color: 'neutral',
      variant: 'ghost',
      label: 'Name',
      icon: isSorted
          ? isSorted === 'asc'
              ? 'i-lucide-arrow-up-narrow-wide'
              : 'i-lucide-arrow-down-wide-narrow'
          : 'i-lucide-arrow-up-down',
      class: '-mx-2.5',
      onClick: () => column.toggleSorting(column.getIsSorted() === 'asc')
    })
  },
  cell: ({ row }) => {
    return h('div', {
      class: 'flex items-center gap-2 cursor-pointer',
      onClick: () => navigate(row.original)
    }, [
      h(UIcon, {
        name: row.original.isDirectory ? 'i-lucide-folder' : 'i-lucide-file',
        class: 'w-5 h-5 text-gray-500'
      }),
      h('span', row.original.name),
      row.original.readOnly && !row.original.isDirectory ? h(UIcon, {
        name: 'i-lucide-lock',
        class: 'w-4 h-4 text-red-400'
      }) : null
    ])
  }
}, {
  accessorKey: 'size',
  sortingFn: directoryFirstSort((f) => f.size),
  header: ({ column }) => {
    const isSorted = column.getIsSorted()

    return h(UButton, {
      color: 'neutral',
      variant: 'ghost',
      label: 'Size',
      icon: isSorted
          ? isSorted === 'asc'
              ? 'i-lucide-arrow-up-narrow-wide'
              : 'i-lucide-arrow-down-wide-narrow'
          : 'i-lucide-arrow-up-down',
      class: '-mx-2.5',
      onClick: () => column.toggleSorting(column.getIsSorted() === 'asc')
    })
  },
  cell: ({ row }) => row.original.isDirectory ? '-' : formatSize(row.original.size)
}, {
  accessorKey: 'lastModified',
  sortingFn: directoryFirstSort((f) => new Date(f.lastModified).getTime()),
  header: ({ column }) => {
    const isSorted = column.getIsSorted()

    return h(UButton, {
      color: 'neutral',
      variant: 'ghost',
      label: 'Last Modified',
      icon: isSorted
          ? isSorted === 'asc'
              ? 'i-lucide-arrow-up-narrow-wide'
              : 'i-lucide-arrow-down-wide-narrow'
          : 'i-lucide-arrow-up-down',
      class: '-mx-2.5',
      onClick: () => column.toggleSorting(column.getIsSorted() === 'asc')
    })
  },
  cell: ({ row }) => formatDate(row.original.lastModified)
}, {
  id: 'actions',
  enableHiding: false,
  meta: {
    class: {
      td: 'text-right'
    }
  },
  cell: ({ row }) => {
    return h(
        UDropdownMenu,
        {
          content: {
            align: 'end'
          },
          items: getActions(row.original),
          'aria-label': 'Actions dropdown'
        },
        () =>
            h(UButton, {
              icon: 'i-lucide-ellipsis-vertical',
              color: 'neutral',
              variant: 'ghost',
              'aria-label': 'Actions dropdown'
            })
    )
  }
}]
</script>

<template>
  <div 
    class="flex flex-col gap-4 relative"
    @dragenter="onDragEnter"
    @dragover.prevent
    @dragleave="onDragLeave"
    @drop="onDrop"
  >
    <div 
      v-if="isDragging" 
      class="absolute inset-0 z-50 bg-primary-500/10 border-2 border-dashed border-primary-500 rounded-lg flex items-center justify-center backdrop-blur-sm"
    >
      <div class="flex flex-col items-center gap-2 text-primary-500">
        <UIcon name="i-lucide-upload-cloud" class="w-12 h-12" />
        <span class="text-lg font-semibold">Drop files to upload to {{ currentPath || 'root' }}</span>
      </div>
    </div>

    <div class="flex items-center gap-2">
      <UButton
        icon="i-lucide-arrow-left"
        variant="ghost"
        :disabled="!currentPath"
        @click="goBack"
      />
      <div class="flex items-center gap-1">
        <template v-for="(bc, index) in getBreadcrumbs" :key="bc.path">
          <UButton
            :label="bc.label"
            variant="link"
            class="px-1"
            @click="navigateToBreadcrumb(bc.path)"
          />
          <span v-if="index < getBreadcrumbs.length - 1" class="text-gray-500">/</span>
        </template>
      </div>
    </div>

    <UCard :ui="{ body: 'p-0' }">
      <UTable
        v-model:sorting="sorting"
        :loading="loading"
        :data="files"
        :columns="columns"
      />
    </UCard>
  </div>
</template>