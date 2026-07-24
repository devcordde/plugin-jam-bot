<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const { user, login, logout } = useAuth()

const route = useRoute()

const items = computed<NavigationMenuItem[]>(() => [
  {
    label: 'Dashboard',
    to: '/app/console',
    active: route.path.startsWith('/app/console')
  },
  {
    label: 'Teams',
    to: '/teams',
    active: route.path.startsWith('/teams')
  },
  {
    label: 'Leaderboard',
    to: '/leaderboard',
    active: route.path.startsWith('/leaderboard')
  },
  {
    label: 'Vote',
    to: '/app/vote',
    active: route.path.startsWith('/app/vote')
  },
  {
    label: 'Discord',
    to: 'https://discord.gg/devcordde',
    target: '_blank'
  }
])
</script>

<template>
  <UHeader>
    <template #title>
      <Logo class="h-6 w-auto" />
    </template>

    <UNavigationMenu :items="items" />
    <template #toggle>
      <slot name="toggle" />
    </template>

    <template #right>
      <UColorModeButton />

      <UTooltip text="Open on GitHub" :kbds="['meta', 'G']">
        <UButton
            color="neutral"
            variant="ghost"
            to="https://github.com/devcordde/plugin-jam-bot"
            target="_blank"
            icon="i-simple-icons-github"
            aria-label="GitHub"
        />
      </UTooltip>
    </template>
  </UHeader>
</template>