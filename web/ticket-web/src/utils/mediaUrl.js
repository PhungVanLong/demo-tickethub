const API_BASE_URL = 'http://localhost:8081'

export function resolveMediaUrl(url) {
    if (!url || typeof url !== 'string') return ''
    if (/^https?:\/\//i.test(url)) return url
    if (url.startsWith('/')) return `${API_BASE_URL}${url}`
    return url
}