<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const open = ref(true)

const items: NavigationMenuItem[][] = [
  [
    {
      label: 'Console',
      icon: 'i-lucide-terminal',
      to: '/app/console',
    },
    {
      label: 'Files',
      icon: 'i-lucide-files',
      to: '/app/files',
    },
    {
      label: 'Settings',
      icon: 'i-lucide-settings',
      to: '/app/settings',
    },
  ],
  [
    {
      label: 'Team',
      icon: 'i-lucide-users',
      to: '/app/team',
    }
  ]
]
</script>

<template>
  <div>
    <UHeader toggle-side="left" :ui="{ container: 'px-4! mx-0! max-w-full!' }" :title="$route.meta.title as string || 'Plugin Jam'">
      <template #right>
        <UserDropdown />
      </template>
      <template #toggle>
        <UButton
            icon="i-lucide-panel-left"
            color="neutral"
            variant="ghost"
            aria-label="Toggle sidebar"
            @click="open = !open"
        />
      </template>
    </UHeader>

    <div class="flex flex-1 min-h-0">
      <USidebar
          v-model:open="open"
          collapsible="icon"
          :ui="{
          gap: 'h-[calc(100%-var(--ui-header-height))]',
          container:
            'absolute top-(--ui-header-height) bottom-0 h-[calc(100%-var(--ui-header-height))]'
        }"
      >
        <div class="flex flex-col gap-2">
          <UNavigationMenu
              :items="items[0]"
              orientation="vertical"
          />
          <USeparator/>
          <UNavigationMenu
              :items="items[1]"
              orientation="vertical"
          />
        </div>
      </USidebar>
      <div class="p-5 w-full max-w-400">
        <slot />
      </div>
    </div>
  </div>
</template>
