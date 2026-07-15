export default defineNuxtRouteMiddleware(async (to, from) => {
  if (!to.path.startsWith('/app') || to.path === '/app/setup') {
    return
  }

  const { user, fetchUser, login } = useAuth()
  const config = useRuntimeConfig()

  if (!user.value) {
    await fetchUser()
  }

  if (!user.value) {
    login()
    return navigateTo('/');
  }

  try {
    const team = await $fetch('/api/teams/my-team', {
      baseURL: config.public.apiBase,
      credentials: 'include',
      headers: useRequestHeaders(['cookie'])
    }).catch(() => null)

    console.log('Team:', team)

    if (!team) {
      return navigateTo('/app/setup')
    }

    const serverInfo = await $fetch<{ exists: boolean }>('/api/server/exists', {
      baseURL: config.public.apiBase,
      credentials: 'include',
      headers: useRequestHeaders(['cookie'])
    })

    if (!serverInfo.exists) {
      return navigateTo('/app/setup')
    }
  } catch (e) {
    console.error('Middleware check failed', e)
    return navigateTo('/app/setup')
  }
})
