import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

function actionLogPlugin() {
    return {
        name: 'action-log-plugin',
        configureServer(server) {
            server.middlewares.use('/__action-log', (req, res, next) => {
                if (req.method !== 'POST') {
                    next()
                    return
                }

                let raw = ''
                req.on('data', (chunk) => {
                    raw += chunk
                })

                req.on('end', () => {
                    try {
                        const data = JSON.parse(raw || '{}')
                        const ts = data.ts || new Date().toISOString()
                        const action = data.action || 'UNKNOWN_ACTION'
                        const route = data.path || '-'
                        const payload = JSON.stringify(data.payload || {})
                        console.log(`[ACTION] ${ts} | ${action} | ${route} | payload=${payload}`)
                    } catch {
                        console.log(`[ACTION] invalid payload: ${raw}`)
                    }
                    res.statusCode = 204
                    res.end()
                })
            })
        },
    }
}

export default defineConfig({
    plugins: [vue(), actionLogPlugin()],
    server: {
        host: true,
        allowedHosts: ['nonfecund-unvenerative-judi.ngrok-free.dev'],
        proxy: {
            '/api': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                rewrite: (path) => path
            }
        }
    },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
})
