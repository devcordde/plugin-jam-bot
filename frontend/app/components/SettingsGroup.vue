<script setup lang="ts">
const props = defineProps<{ title: string, description?: string, color?: string, backgroundColor?: string}>()
</script>

<template>
<div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6" :class="props.backgroundColor ? `bg-${props.backgroundColor}-500` : ''">
  <h3 class="text-2xl font-semibold mb-2" :class="props.color ? `text-${props.color}-500` : 'text-gray-900 dark:text-white'">{{ title }}</h3>
  <p v-if="description" class="text-sm text-gray-500 dark:text-gray-400">{{ description }}</p>
  <div class="flex flex-col gap-4 py-2">
    <slot />
  </div>
</div>
</template>

<style scoped>

</style>