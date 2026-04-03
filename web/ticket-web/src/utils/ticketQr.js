import QRCode from 'qrcode'

function sanitizeFileName(value) {
    return String(value || 'ticket').replace(/[^a-zA-Z0-9_-]/g, '_')
}

export async function createTicketQrDataUrl(qrCodeData, options = {}) {
    if (!qrCodeData) return ''

    return QRCode.toDataURL(String(qrCodeData), {
        width: options.width ?? 320,
        margin: options.margin ?? 1,
        color: {
            dark: options.darkColor ?? '#111827',
            light: options.lightColor ?? '#FFFFFFFF',
        },
    })
}

export async function downloadTicketQrPng(ticket, options = {}) {
    const dataUrl = await createTicketQrDataUrl(ticket?.qrCodeData, options)
    if (!dataUrl) return ''

    const link = document.createElement('a')
    link.href = dataUrl
    link.download = `${sanitizeFileName(ticket?.ticketCode || ticket?.id)}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    return dataUrl
}