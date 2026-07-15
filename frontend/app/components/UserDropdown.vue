<script setup lang="ts">
const { user, logout } = useAuth()
const items = computed(() => [
  [
    {
      label: user.value?.name || 'User',
      avatar: {
        src: user.value?.picture
      },
      disabled: true
    }
  ],
  [
    {
      label: 'Logout',
      icon: 'i-lucide-log-out',
      onSelect() {
        logout()
      }
    }
  ]
])
</script>

<template>
  <template v-if="user">
    <UDropdownMenu :items="items" :popper="{ placement: 'bottom-end' }">
      <UButton color="neutral" variant="ghost" class="flex items-center gap-2">
        <UAvatar :src="user.picture" :alt="user.name" size="sm" />
        <span class="hidden sm:inline">{{ user.name }}</span>
      </UButton>
    </UDropdownMenu>
  </template>
</template>

<style scoped>

</style>