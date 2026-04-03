export function logAction(action, payload = {}) {
    if (!import.meta.env.DEV) return

    const body = {
        action,
        payload,
        path: window.location.pathname,
        ts: new Date().toISOString(),
    }

    fetch('/__action-log', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    }).catch(() => {
        // Never block user flow because of debug logging.
    })
}
