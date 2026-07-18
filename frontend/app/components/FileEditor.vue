<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { EditorView, basicSetup } from 'codemirror'
import { EditorState, Compartment } from '@codemirror/state'
import { yaml } from '@codemirror/lang-yaml'
import { json } from '@codemirror/lang-json'
import { oneDark } from '@codemirror/theme-one-dark'

const props = defineProps<{
  filename: string
  readOnly?: boolean
}>()

const modelValue = defineModel<string>()

const editorContainer = ref<HTMLDivElement>()
let view: EditorView
const languageCompartment = new Compartment()
const readOnlyCompartment = new Compartment()

const getLanguageExtension = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  switch (ext) {
    case 'yml':
    case 'yaml':
      return yaml()
    case 'json':
      return json()
    default:
      return []
  }
}

onMounted(() => {
  const state = EditorState.create({
    doc: modelValue.value,
    extensions: [
      basicSetup,
      oneDark,
      languageCompartment.of(getLanguageExtension(props.filename)),
      readOnlyCompartment.of(EditorView.editable.of(!props.readOnly)),
      EditorState.readOnly.of(props.readOnly),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          modelValue.value = update.state.doc.toString()
        }
      })
    ]
  })

  view = new EditorView({
    state,
    parent: editorContainer.value
  })
})

watch(() => props.filename, (newFilename) => {
  view.dispatch({
    effects: languageCompartment.reconfigure(getLanguageExtension(newFilename))
  })
})



watch(() => modelValue.value, (newValue) => {
  const currentValue = view.state.doc.toString()
  if (newValue !== currentValue) {
    view.dispatch({
      changes: { from: 0, to: currentValue.length, insert: newValue }
    })
  }
})

onBeforeUnmount(() => {
  view.destroy()
})
</script>

<template>
  <div ref="editorContainer" class="cm-wrapper" />
</template>

<style scoped>
.cm-wrapper :deep(.cm-editor) {
  height: 100%;
  border-radius: 0.375rem;
}

:deep(.cm-lineNumbers) {
  color: var(--color-primary);
}

:deep(.cm-editor), :deep(.cm-gutters) {
  background: transparent !important;
}
.cm-wrapper :deep(.cm-scroller) {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.875rem;
}
</style>