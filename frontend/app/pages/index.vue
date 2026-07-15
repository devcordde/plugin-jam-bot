<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-3xl w-full text-center space-y-8">
        <div>
          <h1 class="text-4xl sm:text-6xl font-extrabold tracking-tight text-gray-900 dark:text-white">
            Plugin Jam
          </h1>
          <p class="mt-6 text-lg sm:text-xl text-gray-600 dark:text-gray-400">
            Welcome to the Plugin Jam! This is a creative event where developers come together to build amazing plugins in a limited time. Whether you're a seasoned pro or just starting out, join us to learn, collaborate, and showcase your skills.
          </p>
        </div>
        <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <UButton
            to="/app/console"
            size="xl"
            color="primary"
            icon="i-heroicons-cpu-chip"
          >
            Go to Dashboard
          </UButton>
          <UButton
            to="https://discord.gg/devcordde"
            target="_blank"
            size="xl"
            color="neutral"
            variant="soft"
            icon="i-simple-icons-discord"
          >
            Join the Discord
          </UButton>
        </div>
      </div>
    </main>
  </div>
</template>
