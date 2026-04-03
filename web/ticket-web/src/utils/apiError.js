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

    // Common bean-validation shape: { errors: { fieldName: 'message' } }
    if (data.errors && typeof data.errors === 'object' && !Array.isArray(data.errors)) {
        const firstEntry = Object.entries(data.errors).find(([, value]) => value != null && value !== '')
        if (firstEntry) {
            const [field, value] = firstEntry
            if (Array.isArray(value) && value.length) return `${field}: ${value[0]}`
            if (typeof value === 'string') return `${field}: ${value}`
        }
    }

    // Spring-like validation details: [{ field, defaultMessage }]
    if (Array.isArray(data.violations) && data.violations.length) {
        const first = data.violations[0]
        if (first?.field && first?.message) return `${first.field}: ${first.message}`
        if (first?.field && first?.defaultMessage) return `${first.field}: ${first.defaultMessage}`
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
