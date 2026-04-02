function readBackendMessage(data) {
    if (!data) return ''

    if (typeof data === 'string') return data
    if (typeof data.message === 'string' && data.message.trim()) return data.message
    if (typeof data.error === 'string' && data.error.trim()) return data.error
    if (typeof data.detail === 'string' && data.detail.trim()) return data.detail
    if (typeof data.title === 'string' && data.title.trim()) return data.title

    if (Array.isArray(data.errors) && data.errors.length) {
        const first = data.errors[0]
        if (typeof first === 'string') return first
        if (first?.message) return String(first.message)
    }

    return ''
}

export function extractApiError(error, fallback = 'Request failed') {
    const status = error?.response?.status || null
    const data = error?.response?.data
    const code = data?.code || data?.errorCode || data?.error || null

    let message = readBackendMessage(data)
    if (!message) message = error?.message || fallback
    if (!message) message = fallback

    if (status && status >= 500) {
        message = `Server error (${status}): ${message}`
    } else if (status) {
        message = `Request error (${status}): ${message}`
    }

    return {
        status,
        code,
        message,
        raw: data,
    }
}
