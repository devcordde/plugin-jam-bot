<script setup lang="ts">
const props = defineProps<{ colorLightClass: string, colorDarkClass: string }>()
</script>
<template>
  <span class="relative flex size-3">
        <span class="absolute inline-flex h-full w-full animate-ping rounded-full opacity-75"
              :class="props.colorLightClass"></span>
        <span class="relative inline-flex size-3 rounded-full" :class="props.colorDarkClass"></span>
      </span>
</template>