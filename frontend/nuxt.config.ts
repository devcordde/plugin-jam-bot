// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    modules: ['@nuxt/eslint', '@nuxt/ui', '@pinia/nuxt'],

    devtools: {
        enabled: true
    },

    css: ['~/assets/css/main.css'],

    routeRules: {
        '/': {prerender: true}
    },

    compatibilityDate: '2026-06-30',

    runtimeConfig: {
        public: {
            apiBase: 'http://localhost:8000'
        }
    },

    ui: {
        theme: {
            colors: [
                'primary',
                'secondary',
                'tertiary',
                'info',
                'success',
                'warning',
                'error',
                'brandSuccess',
                'brandWarning',
                'brandError',
            ]
        }
    },

    fonts: {
        families: [
            {name: 'Comic Relief', provider: 'google'},
            {name: 'Baloo Paaji 2', provider: 'google'},
            {name: 'DynaPuff', provider: 'google'},
            {name: 'Open Sans', provider: 'google'},
        ]
    },

    eslint: {
        config: {
            stylistic: {
                commaDangle: 'never',
                braceStyle: '1tbs'
            }
        }
    }
})