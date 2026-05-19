import { useState, useRef } from 'react'
import { generateQuestions } from '../api'
import './Home.css'

export default function Home() {
  const [jobTitle, setJobTitle] = useState('')
  const [result, setResult]     = useState(null)
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)
  const inputRef                = useRef(null)

  async function handleSubmit(e) {
    e.preventDefault()
    const value = jobTitle.trim()
    if (!value) return

    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const data = await generateQuestions(value)
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function handleReset() {
    setResult(null)
    setError(null)
    setJobTitle('')
    setTimeout(() => inputRef.current?.focus(), 50)
  }

  return (
    <main className="home">
      <section className="hero">
        <p className="hero-eyebrow">AI-powered · Role-specific</p>
        <h1 className="hero-heading">
          Interview questions<br />
          <em>that actually fit the role.</em>
        </h1>
        <p className="hero-sub">
          Enter a job title. Get 3 thoughtful, targeted questions in seconds.
        </p>
      </section>

      <section className="form-section">
        <form className="form-card" onSubmit={handleSubmit} noValidate>
          <label className="field-label" htmlFor="jobTitle">Job title</label>
          <div className="input-row">
            <input
              ref={inputRef}
              id="jobTitle"
              type="text"
              className="text-input"
              placeholder="e.g. Senior Product Manager"
              value={jobTitle}
              onChange={e => setJobTitle(e.target.value)}
              disabled={loading}
              maxLength={200}
              autoComplete="off"
              autoFocus
            />
            <button
              type="submit"
              className={`submit-btn ${loading ? 'submit-btn--loading' : ''}`}
              disabled={!jobTitle.trim()}
              aria-busy={loading}
            >
              {loading ? <><SpinnerIcon /> Generating…</> : 'Generate'}
            </button>
          </div>
          <p className="char-count">{jobTitle.length} / 200</p>
        </form>

        {error && (
          <div className="error-banner" role="alert">
            <AlertIcon />
            <span>{error}</span>
          </div>
        )}
      </section>

      {loading && <SkeletonResults />}

      {result && !loading && (
        <section className="results-section">
          <div className="results-meta">
            <span className="results-tag">Generated</span>
            <h2 className="results-role">{result.jobTitle}</h2>
          </div>
          <ol className="question-list">
            {result.questions.map((q, i) => (
              <li key={i} className="question-item" style={{ '--i': i }}>
                <span className="q-index">{String(i + 1).padStart(2, '0')}</span>
                <p className="q-text">{q.replace(/^\d+[.)]\s*/, '')}</p>
              </li>
            ))}
          </ol>
          <button className="try-again-btn" onClick={handleReset} type="button">
            Try another role →
          </button>
        </section>
      )}
    </main>
  )
}

function SkeletonResults() {
  return (
    <section className="results-section skeleton-section" aria-label="Generating questions…" aria-busy="true">
      <div className="results-meta">
        <span className="skeleton-tag" />
        <span className="skeleton-title" />
      </div>
      <div className="question-list">
        {[0, 1, 2].map(i => (
          <div key={i} className="question-item skeleton-item" style={{ '--i': i }}>
            <span className="skeleton-num" />
            <div className="skeleton-lines">
              <span className="skeleton-line" style={{ width: '90%' }} />
              <span className="skeleton-line" style={{ width: '65%' }} />
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

function SpinnerIcon() {
  return (
    <svg className="spin" width="15" height="15" viewBox="0 0 15 15" fill="none" aria-hidden="true">
      <circle cx="7.5" cy="7.5" r="5.5" stroke="currentColor" strokeOpacity=".3" strokeWidth="2" />
      <path d="M13 7.5a5.5 5.5 0 0 0-5.5-5.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function AlertIcon() {
  return (
    <svg width="15" height="15" viewBox="0 0 15 15" fill="none" aria-hidden="true">
      <circle cx="7.5" cy="7.5" r="6.5" stroke="currentColor" strokeWidth="1.5" />
      <path d="M7.5 4v4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <circle cx="7.5" cy="10.5" r=".75" fill="currentColor" />
    </svg>
  )
}
