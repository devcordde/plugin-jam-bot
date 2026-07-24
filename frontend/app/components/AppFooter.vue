<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const items: NavigationMenuItem[] = [
  {
    label: 'Imprint',
    to: '/legal/imprint',
  },
  {
    label: 'Privacy',
    to: '/legal/privacy',
  }
]
</script>

<template>
  <UFooter>
    <template #left>
      <UNavigationMenu :items="items" variant="link" />
    </template>

    <template #right>
      <UButton
          icon="i-simple-icons-discord"
          color="neutral"
          variant="ghost"
          to="https://discord.gg/devcordde"
          target="_blank"
          aria-label="Discord"
      />
      <UButton
          icon="i-simple-icons-github"
          color="neutral"
          variant="ghost"
          to="https://github.com/devcordde/plugin-jam-bot"
          target="_blank"
          aria-label="GitHub"
      />
    </template>
    <template #bottom>
      <div class="flex justify-center items-center">
        <p class="text-muted text-sm">Copyright © {{ new Date().getFullYear() }} DevCord</p>
      </div>
    </template>
  </UFooter>
</template>