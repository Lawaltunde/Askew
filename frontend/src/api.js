const BASE = import.meta.env.VITE_API_URL ?? ''

export async function generateQuestions(jobTitle) {
  const res = await fetch(`${BASE}/api/v1/interviews/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ jobTitle }),
  })

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    const message = body?.message
    throw new Error(
      message ?? (res.status >= 500
        ? 'Something went wrong. Try again in a moment.'
        : `Request failed (${res.status})`)
    )
  }

  return res.json()
}

export async function fetchHistory() {
  const res = await fetch(`${BASE}/api/v1/interviews`)
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new Error(body?.message ?? `Failed to load history (${res.status})`)
  }
  return res.json()
}
