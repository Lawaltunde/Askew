import { Routes, Route } from 'react-router-dom'
import Nav from './components/Nav'
import Home from './pages/Home'
import History from './pages/History'
import './App.css'

export default function App() {
  return (
    <div className="shell">
      <Nav />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/history" element={<History />} />
      </Routes>
      <footer className="site-footer">
        <span className="footer-brand">askew</span>
        <span className="footer-divider" aria-hidden="true">·</span>
        <span>Built with Spring AI + OpenAI</span>
        <span className="footer-divider" aria-hidden="true">·</span>
        <span>© {new Date().getFullYear()} Lawaltunde</span>
      </footer>
    </div>
  )
}
