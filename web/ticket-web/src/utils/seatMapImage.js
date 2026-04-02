const SEATMAP_IMAGE_TAG_REGEX = /\[SEATMAP_IMAGE\]([\s\S]*?)\[\/SEATMAP_IMAGE\]/i

export function extractSeatMapImageFromDescription(description) {
    if (!description || typeof description !== 'string') return ''
    const match = description.match(SEATMAP_IMAGE_TAG_REGEX)
    return match?.[1]?.trim() || ''
}

export function stripSeatMapImageTag(description) {
    if (!description || typeof description !== 'string') return ''
    return description.replace(SEATMAP_IMAGE_TAG_REGEX, '').trim()
}

export function embedSeatMapImageInDescription(description, imageUrl) {
    const cleanDescription = stripSeatMapImageTag(description || '')
    const cleanImageUrl = (imageUrl || '').trim()

    if (!cleanImageUrl) return cleanDescription
    if (!cleanDescription) return `[SEATMAP_IMAGE]${cleanImageUrl}[/SEATMAP_IMAGE]`

    return `${cleanDescription}\n\n[SEATMAP_IMAGE]${cleanImageUrl}[/SEATMAP_IMAGE]`
}