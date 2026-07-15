<script setup lang="ts">
const { user, login, logout } = useAuth()
</script>

<template>
  <header class="border-b border-gray-200 dark:border-gray-800 p-4 flex justify-between items-center bg-white dark:bg-gray-900">
    <div class="flex items-center gap-2">
      <NuxtLink to="/" class="text-xl font-bold">
        Plugin Jam
      </NuxtLink>
    </div>

    <div class="flex items-center gap-4">
      <template v-if="user">
        <UserDropdown />
      </template>
      <template v-else>
        <UButton @click="login" color="primary">
          Login with Discord
        </UButton>
      </template>
    </div>
  </header>
</template>
