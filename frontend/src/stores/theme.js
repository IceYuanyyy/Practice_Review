import { defineStore } from 'pinia'
import { darkTheme } from 'naive-ui'

export const useThemeStore = defineStore('theme', {
    state: () => ({
        isDark: false
    }),

    getters: {
        naiveTheme: (state) => state.isDark ? darkTheme : null,

        // 动态调整主题变量
        themeOverrides: (state) => {
            const common = state.isDark ? {
                primaryColor: '#10b981', // Emerald 500
                primaryColorHover: '#34d399',
                primaryColorPressed: '#059669',
                primaryColorSuppl: '#34d399',

                // Dark Mode Base Colors
                bodyColor: '#0f172a', // Slate 900
                cardColor: '#1e293b', // Slate 800
                modalColor: '#1e293b',
                popoverColor: '#1e293b',
                textColorBase: '#f1f5f9',
                textColor1: '#f1f5f9',
                textColor2: '#cbd5e1',
                textColor3: '#94a3b8',
                borderColor: '#334155'
            } : {
                primaryColor: '#10b981',
                primaryColorHover: '#34d399',
                primaryColorPressed: '#059669',
                primaryColorSuppl: '#34d399',

                // Light Mode Base Colors need to be explicit if we want total control,
                // but Naive UI defaults are usually fine.
                // We just ensure consistency with main.css
            }

            return {
                common: {
                    ...common,
                    borderRadius: '8px',
                    fontFamily: '"Inter", "Roboto", -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", Arial, sans-serif'
                },
                Button: {
                    borderRadiusMedium: '8px',
                    fontWeight: '500'
                },
                Card: {
                    borderRadius: '16px',
                    boxShadow: '0 4px 20px 0 rgba(0, 0, 0, 0.03)'
                },
                Input: {
                    borderRadius: '8px'
                }
            }
        }
    },

    actions: {
        toggleTheme() {
            this.isDark = !this.isDark
            this.applyTheme()
        },

        setTheme(isDark) {
            this.isDark = isDark
            this.applyTheme()
        },

        applyTheme() {
            // Toggle class on document element for tailwind/global css support
            if (this.isDark) {
                document.documentElement.classList.add('dark')
            } else {
                document.documentElement.classList.remove('dark')
            }
        }
    },

    persist: {
        paths: ['isDark'],
        afterRestore: (ctx) => {
            ctx.store.applyTheme()
        }
    }
})
