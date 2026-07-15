export const useAuth = () => {
  const user = useState<User | null>('user', () => null)
  const config = useRuntimeConfig()

  const fetchUser = async () => {
    try {
      const headers = useRequestHeaders(['cookie'])
      const data = await $fetch<User>('/api/user/me', {
        baseURL: config.public.apiBase,
        credentials: 'include',
        headers
      })
      console.log('User data:', data)
      user.value = data
    } catch (e) {
      user.value = null
    }
  }

  const login = () => {
    window.location.href = `${config.public.apiBase}/oauth/login/discord`
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
      window.location.href = '/'
    }
  }

  return {
    user,
    fetchUser,
    login,
    logout
  }
}
