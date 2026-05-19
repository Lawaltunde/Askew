import { useState, useEffect } from 'react'
import { fetchHistory } from '../api'
import './History.css'

export default function History() {
  const [items, setItems]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)
  const [expanded, setExpanded] = useState(null)

  useEffect(() => {
    fetchHistory()
      .then(setItems)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  function toggle(id) {
    setExpanded(prev => prev === id ? null : id)
  }

  function formatDate(iso) {
    return new Date(iso).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    })
  }

  return (
    <main className="history">
      <section className="history-header">
        <h1 className="history-title">Past interviews</h1>
        <p className="history-sub">Every generation is saved here for reference.</p>
      </section>

      {loading && (
        <div className="skeleton-list">
          {[1, 2, 3].map(i => <div key={i} className="skeleton-row" />)}
        </div>
      )}

      {error && (
        <div className="error-banner" role="alert">{error}</div>
      )}

      {!loading && !error && items.length === 0 && (
        <div className="empty-state">
          <p className="empty-title">No interviews yet</p>
          <p className="empty-sub">Head to Generate to create your first set of questions.</p>
        </div>
      )}

      {!loading && items.length > 0 && (
        <div className="history-count">{items.length} interview{items.length !== 1 ? 's' : ''}</div>
      )}

      <ul className="history-list">
        {items.map((item, idx) => (
          <li key={item.id} className="history-item" style={{ '--idx': idx }}>
            <button
              className="item-header"
              onClick={() => toggle(item.id)}
              aria-expanded={expanded === item.id}
            >
              <div className="item-left">
                <span className="item-num">{String(idx + 1).padStart(2, '0')}</span>
                <span className="item-role">{item.jobTitle}</span>
              </div>
              <div className="item-right">
                <span className="item-date">{formatDate(item.createdAt)}</span>
                <ChevronIcon open={expanded === item.id} />
              </div>
            </button>

            {expanded === item.id && (
              <ol className="item-questions">
                {item.questions.map((q, i) => (
                  <li key={i} className="item-question">
                    <span className="item-q-num">{i + 1}</span>
                    <span>{q.replace(/^\d+[.)]\s*/, '')}</span>
                  </li>
                ))}
              </ol>
            )}
          </li>
        ))}
      </ul>
    </main>
  )
}

function ChevronIcon({ open }) {
  return (
    <svg
      className={`chevron ${open ? 'open' : ''}`}
      width="14" height="14" viewBox="0 0 14 14" fill="none"
    >
      <path d="M3 5l4 4 4-4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}
