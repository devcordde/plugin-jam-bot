export const useAuth = () => {
  const user = useState<User | null>('user', () => null)
  const config = useRuntimeConfig()

  const fetchUser = async () => {
    try {
      const headers = useRequestHeaders(['cookie'])
      user.value = await $fetch<User>('/api/user/me', {
        baseURL: config.public.apiBase,
        credentials: 'include',
        headers
      })
    } catch (e) {
      user.value = null
    }
  }

  const login = () => {
     navigateTo(`${config.public.apiBase}/oauth/login/discord`, {
       external: true,
     })
  }

  const logout = async () => {
    try {
      await $fetch('/logout', {
        method: 'POST',
        baseURL: config.public.apiBase,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include'
      })
    } finally {
      user.value = null
      navigateTo('/')
    }
  }

  return {
    user,
    fetchUser,
    login,
    logout
  }
}
